package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WorldlyTutor.class, FeralShadow.class, Disenchant.class, Island.class})
class WorldlyTutorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers only creature cards from the library")
    void offersOnlyCreatures() {
        List<Card> deck = setupLibrary();
        cast();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(deck.getFirst());
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.TOP_OF_LIBRARY);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing a creature puts it on top of the library")
    void choosingCreaturePutsOnTop() {
        List<Card> deck = setupLibrary();
        cast();
        harness.passBothPriorities();

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        Card chosen = offered.getFirst();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(deck);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("reveals")).isTrue();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Failing to find is allowed")
    void failToFindIsAllowed() {
        List<Card> deck = setupLibrary();
        cast();
        harness.passBothPriorities();

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore)
                .containsExactlyInAnyOrderElementsOf(deck);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No interaction when the library has no creatures")
    void noCreaturesNoInteraction() {
        harness.setLibrary(player1, List.of(new Disenchant(), new Island()));

        cast();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    private void cast() {
        harness.castFromHand(player1, new WorldlyTutor(), "{G}");
    }

    private List<Card> setupLibrary() {
        List<Card> deck = List.of(new FeralShadow(), new Disenchant(), new Island());
        harness.setLibrary(player1, deck);
        return deck;
    }
}
