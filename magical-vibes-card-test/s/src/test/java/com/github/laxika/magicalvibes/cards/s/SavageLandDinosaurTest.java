package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SavageLandDinosaur.class, Forest.class})
class SavageLandDinosaurTest extends BaseCardTest {

    @Test
    @DisplayName("Basic landcycling searches for a basic land and discards the card")
    void basicLandcyclingSearchesForBasicLand() {
        Forest forest = new Forest();
        SavageLandDinosaur nonland = new SavageLandDinosaur();
        harness.setHand(player1, List.of(new SavageLandDinosaur()));
        harness.setLibrary(player1, List.of(forest, nonland));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Savage Land Dinosaur");
        harness.assertInHand(player1, "Forest");
        harness.assertNotInHand(player1, "Savage Land Dinosaur");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
    }

    @Test
    @DisplayName("Basic landcycling discards as a cost before the search resolves")
    void discardsBeforeResolution() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new SavageLandDinosaur()));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Savage Land Dinosaur");
        harness.assertNotInHand(player1, "Savage Land Dinosaur");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Basic landcycling may fail to find even when a basic land is present")
    void mayFailToFind() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new SavageLandDinosaur()));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Savage Land Dinosaur");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Basic landcycling does not draw a nonland when no basic land is present")
    void noMatchingLand() {
        SavageLandDinosaur nonland = new SavageLandDinosaur();
        harness.setHand(player1, List.of(new SavageLandDinosaur()));
        harness.setLibrary(player1, List.of(nonland));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Savage Land Dinosaur");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Basic landcycling requires two mana and does not discard on failed activation")
    void insufficientManaDoesNotDiscard() {
        harness.setHand(player1, List.of(new SavageLandDinosaur()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Savage Land Dinosaur");
        harness.assertNotInGraveyard(player1, "Savage Land Dinosaur");
        assertThat(gd.stack).isEmpty();
    }
}
