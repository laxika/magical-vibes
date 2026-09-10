package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisturbedBurial.class, Disenchant.class, HornedTurtle.class})
class DisturbedBurialTest extends BaseCardTest {

    @Test
    @DisplayName("Without buyback the creature returns to hand and the spell goes to the graveyard")
    void returnsCreatureAndGoesToGraveyard() {
        Card creature = new HornedTurtle();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DisturbedBurial()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, creature.getId());
        assertThat(gd.stack.getFirst().isBuyback()).isFalse();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Horned Turtle");
        harness.assertNotInHand(player1, "Disturbed Burial");
        harness.assertInGraveyard(player1, "Disturbed Burial");
    }

    @Test
    @DisplayName("Disturbed Burial returns only the targeted creature card")
    void returnsOnlyTargetedCreature() {
        Card otherCreature = new HornedTurtle();
        Card targetedCreature = new HornedTurtle();
        harness.setGraveyard(player1, List.of(otherCreature, targetedCreature));
        harness.setHand(player1, List.of(new DisturbedBurial()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, targetedCreature.getId());

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(targetedCreature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(otherCreature.getId())
                .doesNotContain(targetedCreature.getId());
    }

    @Test
    @DisplayName("Paying buyback returns both the creature and Disturbed Burial to hand")
    void buybackReturnsSpellToHand() {
        Card creature = new HornedTurtle();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DisturbedBurial()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorceryWithBuyback(player1, 0, creature.getId());
        assertThat(harness.getGameData().stack.getFirst().isBuyback()).isTrue();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Horned Turtle");
        harness.assertInHand(player1, "Disturbed Burial");
        harness.assertNotInGraveyard(player1, "Disturbed Burial");
    }

    @Test
    @DisplayName("A fizzled buyback spell still goes to the graveyard")
    void fizzledBuybackGoesToGraveyard() {
        Card creature = new HornedTurtle();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DisturbedBurial()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorceryWithBuyback(player1, 0, creature.getId());
        gd.playerGraveyards.get(player1.getId()).removeIf(c -> c.getId().equals(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Disturbed Burial");
    }

    @Test
    @DisplayName("Disturbed Burial cannot target a non-creature card")
    void cannotTargetNonCreature() {
        Card instant = new Disenchant();
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
        Card creature = new HornedTurtle();
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
        Card creature = new HornedTurtle();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DisturbedBurial()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithBuyback(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Disturbed Burial");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }
}
