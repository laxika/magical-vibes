package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.w.WildMongrel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MorgueTheft.class, WildMongrel.class})
class MorgueTheftTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature card from your graveyard to your hand")
    void returnsTargetCreatureFromGraveyardToHand() {
        Card creature = new WildMongrel();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MorgueTheft()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertInHand(player1, "Wild Mongrel");
        harness.assertNotInGraveyard(player1, "Wild Mongrel");
        harness.assertInGraveyard(player1, "Morgue Theft");
    }

    @Test
    @DisplayName("Cannot target a non-creature card in your graveyard")
    void cannotTargetNonCreatureCard() {
        Card nonCreature = new MorgueTheft();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setHand(player1, List.of(new MorgueTheft()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card creature = new WildMongrel();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new MorgueTheft()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Fizzles if the target creature leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Card creature = new WildMongrel();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MorgueTheft()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Wild Mongrel");
    }

    @Test
    @DisplayName("Flashback returns a creature and exiles Morgue Theft")
    void flashbackReturnsCreatureAndExilesSpell() {
        Card theft = new MorgueTheft();
        Card creature = new WildMongrel();
        harness.setGraveyard(player1, List.of(theft, creature));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveFlashback(player1, 0, creature.getId());

        GameData gd = harness.getGameData();
        harness.assertInHand(player1, "Wild Mongrel");
        harness.assertNotInGraveyard(player1, "Morgue Theft");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(theft.getId()));
    }

    @Test
    @DisplayName("Flashback still exiles Morgue Theft when its target becomes illegal")
    void flashbackExilesSpellIfTargetLeavesGraveyard() {
        Card theft = new MorgueTheft();
        Card creature = new WildMongrel();
        harness.setGraveyard(player1, List.of(theft, creature));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castFlashback(player1, 0, creature.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Morgue Theft");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(theft.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flashback cannot be paid with the normal casting cost")
    void flashbackRequiresFullFiveManaCost() {
        Card theft = new MorgueTheft();
        Card creature = new WildMongrel();
        harness.setGraveyard(player1, List.of(theft, creature));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Morgue Theft");
        harness.assertInGraveyard(player1, "Wild Mongrel");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Returns only the chosen creature when several creatures are in the graveyard")
    void returnsOnlyTargetedCreature() {
        Card target = new WildMongrel();
        Card other = new WildMongrel();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setHand(player1, List.of(new MorgueTheft()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId).containsExactly(target.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(other.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));
    }
}
