package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinRecruiterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts choice showing only Goblin cards from the library")
    void etbPromptsChoiceWithOnlyGoblins() {
        setupAndCast();
        Card goblinA = new RagingGoblin();
        Card goblinB = new RagingGoblin();
        setLibrary(List.of(goblinA, new GrizzlyBears(), goblinB, new GrizzlyBears()));

        resolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.SearchLibraryToTopChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryToTopChoice.class).pool())
                .containsExactlyInAnyOrder(goblinA, goblinB);
    }

    @Test
    @DisplayName("Choosing a single Goblin puts it on top without a reorder step")
    void choosingSingleGoblinPutsOnTop() {
        setupAndCast();
        Card goblin = new RagingGoblin();
        setLibrary(List.of(goblin, new GrizzlyBears(), new GrizzlyBears()));

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
        Card goblinA = new RagingGoblin();
        Card goblinB = new RagingGoblin();
        setLibrary(List.of(goblinA, goblinB, new GrizzlyBears()));

        resolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of(goblinA.getId(), goblinB.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        // Put goblinB on top, goblinA second (order [1, 0])
        gs.handleLibraryCardsReordered(gd, player1, List.of(1, 0));

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(3);
        assertThat(library.get(0).getId()).isEqualTo(goblinB.getId());
        assertThat(library.get(1).getId()).isEqualTo(goblinA.getId());
    }

    @Test
    @DisplayName("Choosing zero Goblins leaves all cards in the library")
    void choosingZeroKeepsLibrary() {
        setupAndCast();
        Card goblin = new RagingGoblin();
        setLibrary(List.of(goblin, new GrizzlyBears()));

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
        setLibrary(List.of(new GrizzlyBears(), new GrizzlyBears()));

        resolveEtb();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryToTopChoice.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("finds no Goblin cards"));
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new GoblinRecruiter()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
    }

    private void setLibrary(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private void resolveEtb() {
        harness.passBothPriorities(); // Resolve creature spell → ETB trigger on stack
        harness.passBothPriorities(); // Resolve ETB trigger → search-to-top choice
    }
}
