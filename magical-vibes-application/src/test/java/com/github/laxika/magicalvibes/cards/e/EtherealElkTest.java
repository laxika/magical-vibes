package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.v.VivienNaturesAvenger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EtherealElk.class, VivienNaturesAvenger.class})
class EtherealElkTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Ethereal Elk triggers a may ability")
    void resolvingTriggersMayPrompt() {
        setupAndCast();

        resolveCreature();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may ability returns Vivien, Nature's Avenger from the graveyard")
    void acceptingMayFindsVivienInGraveyard() {
        harness.setGraveyard(player1, List.of(new VivienNaturesAvenger()));
        setupAndCast();

        resolveMay(true);

        harness.assertInHand(player1, "Vivien, Nature's Avenger");
        harness.assertNotInGraveyard(player1, "Vivien, Nature's Avenger");
    }

    @Test
    @DisplayName("Accepting the may ability searches the library when Vivien is not in the graveyard")
    void acceptingMaySearchesLibrary() {
        VivienNaturesAvenger vivien = new VivienNaturesAvenger();
        harness.setLibrary(player1, List.of(vivien));
        setupAndCast();

        resolveMay(true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst().getName()).isEqualTo("Vivien, Nature's Avenger");
    }

    @Test
    @DisplayName("Declining the may ability leaves Vivien, Nature's Avenger in the graveyard")
    void decliningMayDoesNotSearch() {
        harness.setGraveyard(player1, List.of(new VivienNaturesAvenger()));
        setupAndCast();

        resolveMay(false);

        harness.assertInGraveyard(player1, "Vivien, Nature's Avenger");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A library search puts the selected Vivien into hand")
    void librarySearchMovesSelectedCardToHand() {
        VivienNaturesAvenger vivien = new VivienNaturesAvenger();
        harness.setLibrary(player1, List.of(vivien));
        setupAndCast();

        resolveMay(true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(vivien);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller may fail to find Vivien in the library")
    void librarySearchMayFailToFind() {
        VivienNaturesAvenger vivien = new VivienNaturesAvenger();
        harness.setLibrary(player1, List.of(vivien));
        setupAndCast();

        resolveMay(true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(vivien);
    }

    @Test
    @DisplayName("Vivien in both zones requires a choice rather than automatically taking the graveyard copy")
    void matchingCardsInBothZonesRequireChoice() {
        VivienNaturesAvenger graveyardVivien = new VivienNaturesAvenger();
        VivienNaturesAvenger libraryVivien = new VivienNaturesAvenger();
        harness.setGraveyard(player1, List.of(graveyardVivien));
        harness.setLibrary(player1, List.of(libraryVivien));
        setupAndCast();

        resolveMay(true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardVivien);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryVivien);
        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }

    @Test
    @DisplayName("Searching empty zones completes without adding a card")
    void emptyZonesCompleteWithoutFindingCard() {
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of());
        setupAndCast();

        resolveMay(true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new EtherealElk()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
    }

    private void resolveCreature() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void resolveMay(boolean choice) {
        resolveCreature();
        harness.handleMayAbilityChosen(player1, choice);
    }

}
