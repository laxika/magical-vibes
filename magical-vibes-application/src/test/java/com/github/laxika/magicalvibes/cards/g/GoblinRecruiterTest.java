package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.c.CloudElemental;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinRecruiter.class, GoblinSwineRider.class, CloudElemental.class})
class GoblinRecruiterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts choice showing only Goblin cards from the library")
    void etbPromptsChoiceWithOnlyGoblins() {
        setupAndCast();
        Card goblinA = new GoblinSwineRider();
        Card goblinB = new GoblinSwineRider();
        harness.setLibrary(player1, List.of(goblinA, new CloudElemental(), goblinB, new CloudElemental()));

        resolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.SearchLibraryToTopChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryToTopChoice.class).pool())
                .containsExactlyInAnyOrder(goblinA, goblinB);
    }

    @Test
    @DisplayName("Choosing a single Goblin puts it on top without a reorder step")
    void choosingSingleGoblinPutsOnTop() {
        setupAndCast();
        Card goblin = new GoblinSwineRider();
        harness.setLibrary(player1, List.of(goblin, new CloudElemental(), new CloudElemental()));

        resolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of(goblin.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(goblin.getId());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Choosing multiple Goblins triggers a reorder step, then places them on top")
    void choosingMultipleGoblinsReordersOnTop() {
        setupAndCast();
        Card goblinA = new GoblinSwineRider();
        Card goblinB = new GoblinSwineRider();
        harness.setLibrary(player1, List.of(goblinA, goblinB, new CloudElemental()));

        resolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of(goblinA.getId(), goblinB.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        // Put goblinB on top, goblinA second (order [1, 0])
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(3);
        assertThat(library.get(0).getId()).isEqualTo(goblinB.getId());
        assertThat(library.get(1).getId()).isEqualTo(goblinA.getId());
    }

    @Test
    @DisplayName("Choosing some Goblins reveals the choice and leaves the others in the library")
    void choosingSubsetRevealsAndLeavesUnchosenGoblinsInLibrary() {
        setupAndCast();
        Card chosenGoblin = new GoblinSwineRider();
        Card unchosenGoblin = new GoblinSwineRider();
        Card nonGoblin = new CloudElemental();
        harness.setLibrary(player1, List.of(chosenGoblin, unchosenGoblin, nonGoblin));

        resolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of(chosenGoblin.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(3);
        assertThat(library.getFirst().getId()).isEqualTo(chosenGoblin.getId());
        assertThat(library).contains(unchosenGoblin, nonGoblin);
        assertThat(gameLogContains("reveals " + chosenGoblin.getName())).isTrue();
    }

    @Test
    @DisplayName("Choosing zero Goblins leaves all cards in the library")
    void choosingZeroKeepsLibrary() {
        setupAndCast();
        Card goblin = new GoblinSwineRider();
        harness.setLibrary(player1, List.of(goblin, new CloudElemental()));

        resolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(2)
                .anyMatch(c -> c.getId().equals(goblin.getId()));
    }

    @Test
    @DisplayName("No Goblins in library: no prompt, library is shuffled")
    void noGoblinsShufflesLibrary() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new CloudElemental(), new CloudElemental()));

        resolveEtb();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryToTopChoice.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("finds no Goblin cards"));
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new GoblinRecruiter(), "{1}{R}");
    }

    private void resolveEtb() {
        harness.passBothPriorities(); // Resolve creature spell → ETB trigger on stack
        harness.passBothPriorities(); // Resolve ETB trigger → search-to-top choice
    }
}
