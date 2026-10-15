package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FurnaceHostCharger.class, Mountain.class, Forest.class})
class FurnaceHostChargerTest extends BaseCardTest {

    @Test
    @DisplayName("Mountaincycling discards the card and offers only Mountain cards")
    void mountaincyclingSearchesForMountain() {
        harness.setHand(player1, List.of(new FurnaceHostCharger()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLibrary(player1, List.of(new Mountain(), new Forest()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof FurnaceHostCharger);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).hasSize(1);
        assertThat(offered.getFirst()).isInstanceOf(Mountain.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof Mountain);
    }

    @Test
    @DisplayName("Haste allows Furnace Host Charger to attack the turn it is cast")
    void canAttackImmediatelyAfterBeingCast() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FurnaceHostCharger()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        resolveCombat();

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Mountaincycling discards as a cost and accepts generic mana")
    void discardsBeforeResolution() {
        harness.setHand(player1, List.of(new FurnaceHostCharger()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Furnace Host Charger");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Mountaincycling requires two mana before discarding")
    void cannotCycleWithInsufficientMana() {
        harness.setHand(player1, List.of(new FurnaceHostCharger()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Furnace Host Charger");
        harness.assertNotInGraveyard(player1, "Furnace Host Charger");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mountaincycling may fail to find an available Mountain")
    void mayFailToFindMountain() {
        harness.setHand(player1, List.of(new FurnaceHostCharger()));
        harness.setLibrary(player1, List.of(new Mountain(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Furnace Host Charger");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Mountaincycling resolves without drawing when there is no Mountain")
    void resolvesWithoutMountain() {
        harness.setHand(player1, List.of(new FurnaceHostCharger()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Furnace Host Charger");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Mountaincycling resolves with an empty library")
    void resolvesWithEmptyLibrary() {
        harness.setHand(player1, List.of(new FurnaceHostCharger()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Furnace Host Charger");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
