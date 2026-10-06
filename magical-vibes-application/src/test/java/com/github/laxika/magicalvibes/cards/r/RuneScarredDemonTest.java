package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuneScarredDemon.class, Manalith.class})
class RuneScarredDemonTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield offers every card in the library")
    void etbOffersAnyCard() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Rune-Scarred Demon");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(2);
    }

    @Test
    @DisplayName("Chosen card goes to hand and leaves the library")
    void chosenCardGoesToHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Search is unrestricted, so failing to find is not allowed")
    void cannotFailToFind() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isFalse();
    }

    @Test
    @DisplayName("Empty library produces no search prompt")
    void emptyLibraryNoPrompt() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Rune-Scarred Demon");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Search waits for the enters ability to resolve")
    void searchWaitsForTriggerResolution() {
        setupAndCast();
        setupLibrary();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rune-Scarred Demon");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

    @Test
    @DisplayName("An artifact can be chosen without revealing it or affecting the opponent's library")
    void chosenArtifactIsPrivateAndOnlyControllerSearches() {
        setupAndCast();
        Card creature = new RuneScarredDemon();
        Card artifact = new Manalith();
        Card opponentCard = new RuneScarredDemon();
        harness.setLibrary(player1, List.of(creature, artifact));
        harness.setLibrary(player2, List.of(opponentCard));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals()).isFalse();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new RuneScarredDemon()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new RuneScarredDemon(), new Manalith()));
    }
}
