package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DwarvenRecruiterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts choice showing only Dwarf cards from the library")
    void etbPromptsChoiceWithOnlyDwarves() {
        setupAndCast();
        Card dwarfA = new DwarvenGrunt();
        Card dwarfB = new DwarvenGrunt();
        setLibrary(List.of(dwarfA, new GrizzlyBears(), dwarfB, new GrizzlyBears()));

        resolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.SearchLibraryToTopChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryToTopChoice.class).pool())
                .containsExactlyInAnyOrder(dwarfA, dwarfB);
    }

    @Test
    @DisplayName("Choosing a single Dwarf puts it on top without a reorder step")
    void choosingSingleDwarfPutsOnTop() {
        setupAndCast();
        Card dwarf = new DwarvenGrunt();
        setLibrary(List.of(dwarf, new GrizzlyBears(), new GrizzlyBears()));

        resolveEtb();
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
        setLibrary(List.of(dwarfA, dwarfB, new GrizzlyBears()));

        resolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of(dwarfA.getId(), dwarfB.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(3);
        assertThat(library.get(0).getId()).isEqualTo(dwarfB.getId());
        assertThat(library.get(1).getId()).isEqualTo(dwarfA.getId());
    }

    @Test
    @DisplayName("Choosing zero Dwarves leaves all cards in the library")
    void choosingZeroKeepsLibrary() {
        setupAndCast();
        Card dwarf = new DwarvenGrunt();
        setLibrary(List.of(dwarf, new GrizzlyBears()));

        resolveEtb();
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
        setLibrary(List.of(new GrizzlyBears(), new GrizzlyBears()));

        resolveEtb();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryToTopChoice.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("finds no Dwarf cards"));
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new DwarvenRecruiter()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
    }

    private void setLibrary(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private void resolveEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
