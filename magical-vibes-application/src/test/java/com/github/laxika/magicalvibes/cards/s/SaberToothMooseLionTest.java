package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaberToothMooseLion.class, Forest.class})
class SaberToothMooseLionTest extends BaseCardTest {

    @Test
    @DisplayName("Forestcycling discards the card and offers only Forest cards")
    void forestcyclingDiscardsAndOffersForests() {
        harness.setHand(player1, List.of(new SaberToothMooseLion()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new SaberToothMooseLion()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Saber-Tooth Moose-Lion");
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().cards()).allMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Choosing a Forest from Forestcycling puts it into hand")
    void choosingForestPutsItIntoHand() {
        harness.setHand(player1, List.of(new SaberToothMooseLion()));
        harness.setLibrary(player1, List.of(new Forest(), new SaberToothMooseLion()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Saber-Tooth Moose-Lion");
    }

    @Test
    @DisplayName("Forestcycling pays the discard cost before the search resolves")
    void discardIsPaidBeforeResolution() {
        harness.setHand(player1, List.of(new SaberToothMooseLion()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Saber-Tooth Moose-Lion");
        harness.assertInGraveyard(player1, "Saber-Tooth Moose-Lion");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        assertThat(gameLogContains("reveals Forest")).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Forestcycling cannot be activated with only one mana")
    void insufficientManaDoesNotDiscard() {
        harness.setHand(player1, List.of(new SaberToothMooseLion()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Saber-Tooth Moose-Lion");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Forestcycling may fail to find even when a Forest is available")
    void mayFailToFindAvailableForest() {
        harness.setHand(player1, List.of(new SaberToothMooseLion()));
        harness.setLibrary(player1, List.of(new Forest(), new SaberToothMooseLion()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Saber-Tooth Moose-Lion");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Forestcycling resolves without drawing when no Forest is present")
    void noForestDoesNotDrawAnotherCard() {
        harness.setHand(player1, List.of(new SaberToothMooseLion()));
        harness.setLibrary(player1, List.of(new SaberToothMooseLion()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Saber-Tooth Moose-Lion");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Forestcycling can resolve with an empty library")
    void emptyLibraryDoesNotRequireAChoice() {
        harness.setHand(player1, List.of(new SaberToothMooseLion()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Saber-Tooth Moose-Lion");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
