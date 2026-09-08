package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.setGraveyard(player1, List.of(createVivien()));
        setupAndCast();

        resolveMay(true);

        harness.assertInHand(player1, "Vivien, Nature's Avenger");
        harness.assertNotInGraveyard(player1, "Vivien, Nature's Avenger");
    }

    @Test
    @DisplayName("Accepting the may ability searches the library when Vivien is not in the graveyard")
    void acceptingMaySearchesLibrary() {
        Card vivien = createVivien();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(vivien);
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
        harness.setGraveyard(player1, List.of(createVivien()));
        setupAndCast();

        resolveMay(false);

        harness.assertInGraveyard(player1, "Vivien, Nature's Avenger");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
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

    private Card createVivien() {
        Card vivien = new Card();
        vivien.setName("Vivien, Nature's Avenger");
        vivien.setType(CardType.PLANESWALKER);
        vivien.setManaCost("{3}{G}{G}");
        return vivien;
    }
}
