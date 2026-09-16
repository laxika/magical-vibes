package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DwarvenRecruiter.class, DwarvenGrunt.class, AvenFlock.class})
class DwarvenRecruiterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts choice showing only Dwarf cards from the library")
    void etbPromptsChoiceWithOnlyDwarves() {
        setupAndCast();
        Card dwarfA = new DwarvenGrunt();
        Card dwarfB = new DwarvenGrunt();
        harness.setLibrary(player1, List.of(dwarfA, new AvenFlock(), dwarfB, new AvenFlock()));

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.SearchLibraryToTopChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryToTopChoice.class).pool())
                .containsExactlyInAnyOrder(dwarfA, dwarfB);
    }

    @Test
    @DisplayName("Choosing a single Dwarf puts it on top without a reorder step")
    void choosingSingleDwarfPutsOnTop() {
        setupAndCast();
        Card dwarf = new DwarvenGrunt();
        harness.setLibrary(player1, List.of(dwarf, new AvenFlock(), new AvenFlock()));

        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(dwarf.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(dwarf.getId());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Choosing multiple Dwarves triggers a reorder step, then places them on top")
    void choosingMultipleDwarvesReordersOnTop() {
        setupAndCast();
        Card dwarfA = new DwarvenGrunt();
        Card dwarfB = new DwarvenGrunt();
        harness.setLibrary(player1, List.of(dwarfA, dwarfB, new AvenFlock()));

        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(dwarfA.getId(), dwarfB.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(3);
        assertThat(library.get(0).getId()).isEqualTo(dwarfB.getId());
        assertThat(library.get(1).getId()).isEqualTo(dwarfA.getId());
    }

    @Test
    @DisplayName("Choosing a subset reveals the chosen Dwarf and returns the others to the library")
    void choosingSubsetRevealsChosenAndReturnsOthers() {
        setupAndCast();
        Card chosenDwarf = new DwarvenGrunt();
        Card unchosenDwarf = new DwarvenGrunt();
        Card nonDwarf = new AvenFlock();
        harness.setLibrary(player1, List.of(chosenDwarf, unchosenDwarf, nonDwarf));

        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(chosenDwarf.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(3);
        assertThat(library.getFirst()).isSameAs(chosenDwarf);
        assertThat(library).contains(unchosenDwarf, nonDwarf);
        assertThat(gameLogContains("reveals " + chosenDwarf.getName())).isTrue();
    }

    @Test
    @DisplayName("Choosing zero Dwarves leaves all cards in the library")
    void choosingZeroKeepsLibrary() {
        setupAndCast();
        Card dwarf = new DwarvenGrunt();
        harness.setLibrary(player1, List.of(dwarf, new AvenFlock()));

        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(2)
                .anyMatch(c -> c.getId().equals(dwarf.getId()));
    }

    @Test
    @DisplayName("No Dwarves in library: no prompt, library is shuffled")
    void noDwarvesInLibraryShufflesLibrary() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new AvenFlock(), new AvenFlock()));

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryToTopChoice.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("finds no Dwarf cards"));
    }

    @Test
    @DisplayName("An empty library search does not prompt")
    void emptyLibraryDoesNotPrompt() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryToTopChoice.class)).isNull();
        assertThat(gameLogContains("library but it is empty")).isTrue();
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new DwarvenRecruiter(), "{2}{R}");
    }
}
