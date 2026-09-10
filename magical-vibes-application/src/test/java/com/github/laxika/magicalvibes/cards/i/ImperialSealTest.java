package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.r.RavagesOfWar;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImperialSeal.class, Island.class, RavagesOfWar.class, Swamp.class})
class ImperialSealTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers any card from the library")
    void offersAnyCard() {
        setupLibrary();
        cast();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(3);
        assertThat(search.params().reveals()).isFalse();
        assertThat(search.params().canFailToFind()).isFalse();
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.TOP_OF_LIBRARY);
    }

    @Test
    @DisplayName("Choosing a card puts it on top of the library and loses 2 life")
    void choosingCardPutsOnTopAndLosesLife() {
        harness.setLife(player1, 20);
        setupLibrary();
        cast();
        harness.passBothPriorities();

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        Card chosenCard = offered.get(1);

        harness.handleCardChosen(player1, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst().getId()).isEqualTo(chosenCard.getId());
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Life loss happens after the library choice")
    void lifeLossHappensAfterLibraryChoice() {
        harness.setLife(player1, 20);
        setupLibrary();
        cast();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);

        harness.handleCardChosen(player1, 0);

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("An empty library still causes the 2-life loss")
    void emptyLibraryStillLosesLife() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of());
        cast();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Search is mandatory: cannot fail to find")
    void cannotFailToFind() {
        setupLibrary();
        cast();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast() {
        harness.castFromHand(player1, new ImperialSeal(), "{B}");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Island(), new Swamp(), new RavagesOfWar()));
    }
}
