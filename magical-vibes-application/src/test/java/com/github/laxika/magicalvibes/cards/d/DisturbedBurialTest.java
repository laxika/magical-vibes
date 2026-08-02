package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DisturbedBurialTest extends BaseCardTest {

    @Test
    @DisplayName("Without buyback the creature returns to hand and the spell goes to the graveyard")
    void returnsCreatureAndGoesToGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DisturbedBurial()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, creature.getId());
        assertThat(harness.getGameData().stack.getFirst().isBuyback()).isFalse();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(creature.getId()));
        assertThat(handNames(player1)).doesNotContain("Disturbed Burial");
        assertThat(graveyardNames(player1)).contains("Disturbed Burial");
    }

    @Test
    @DisplayName("Paying buyback returns both the creature and Disturbed Burial to hand")
    void buybackReturnsSpellToHand() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DisturbedBurial()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorceryWithBuyback(player1, 0, creature.getId());
        assertThat(harness.getGameData().stack.getFirst().isBuyback()).isTrue();
        harness.passBothPriorities();

        assertThat(handNames(player1)).contains("Grizzly Bears", "Disturbed Burial");
        assertThat(graveyardNames(player1)).doesNotContain("Disturbed Burial");
    }

    @Test
    @DisplayName("A fizzled buyback spell still goes to the graveyard")
    void fizzledBuybackGoesToGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DisturbedBurial()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorceryWithBuyback(player1, 0, creature.getId());
        harness.getGameData().playerGraveyards.get(player1.getId()).removeIf(c -> c.getId().equals(creature.getId()));
        harness.passBothPriorities();

        assertThat(handNames(player1)).isEmpty();
        assertThat(graveyardNames(player1)).contains("Disturbed Burial");
    }

    @Test
    @DisplayName("Disturbed Burial cannot target a non-creature card")
    void cannotTargetNonCreature() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new DisturbedBurial()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Disturbed Burial cannot target a card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new DisturbedBurial()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Announcing buyback without enough mana rewinds the cast")
    void buybackWithoutManaRewinds() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DisturbedBurial()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithBuyback(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(handNames(player1)).containsExactly("Disturbed Burial");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    private List<String> handNames(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerHands.get(player.getId()).stream().map(Card::getName).toList();
    }

    private List<String> graveyardNames(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(Card::getName).toList();
    }
}
