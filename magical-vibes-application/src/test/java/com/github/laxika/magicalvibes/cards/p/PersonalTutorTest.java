package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AncestralMemories;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PersonalTutor.class, AncestralMemories.class, GrizzlyBears.class, Island.class})
class PersonalTutorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers only sorcery cards from the library")
    void offersOnlySorceries() {
        List<Card> deck = setupLibrary();
        cast();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .containsExactly(deck.getLast())
                .allMatch(c -> c.hasType(CardType.SORCERY));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.TOP_OF_LIBRARY);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing a sorcery puts it on top of the library")
    void choosingSorceryPutsOnTop() {
        List<Card> originalDeck = setupLibrary();
        cast();
        harness.passBothPriorities();

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        Card chosen = offered.getFirst();

        harness.handleCardChosen(player1, 0);

        List<Card> deckAfterSearch = gd.playerDecks.get(player1.getId());
        assertThat(deckAfterSearch.getFirst()).isSameAs(chosen);
        assertThat(deckAfterSearch).containsExactlyInAnyOrderElementsOf(originalDeck);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("reveals")).isTrue();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Failing to find is allowed")
    void failToFindIsAllowed() {
        List<Card> originalDeck = setupLibrary();
        cast();
        harness.passBothPriorities();

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore)
                .containsExactlyInAnyOrderElementsOf(originalDeck);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("No interaction when the library has no sorceries")
    void noSorceriesNoInteraction() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Island()));

        cast();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Empty library resolves without an interaction")
    void emptyLibraryNoInteraction() {
        harness.setLibrary(player1, List.of());

        cast();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("library but it is empty")).isTrue();
    }

    private void cast() {
        harness.castFromHand(player1, new PersonalTutor(), "{U}");
    }

    private List<Card> setupLibrary() {
        List<Card> deck = List.of(new GrizzlyBears(), new Island(), new AncestralMemories());
        harness.setLibrary(player1, deck);
        return deck;
    }
}
