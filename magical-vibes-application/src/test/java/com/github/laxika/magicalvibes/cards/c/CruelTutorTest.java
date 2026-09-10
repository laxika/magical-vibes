package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CruelTutor.class, BogImp.class, GrizzlyBears.class, Island.class})
class CruelTutorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers any card from the library")
    void offersAnyCard() {
        setupLibrary();
        cast();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(3);
    }

    @Test
    @DisplayName("Choosing a card puts it on top of the library and loses 2 life")
    void choosingCardPutsOnTopAndLosesLife() {
        harness.setLife(player1, 20);
        setupLibrary();
        cast();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.get(1).getName();

        // Effects resolve in oracle order: the life loss waits behind the search and only
        // resolves when the search completes and the paused resolution resumes.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        harness.handleCardChosen(player1, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst().getName()).isEqualTo(chosenName);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("Search is mandatory: cannot fail to find")
    void cannotFailToFind() {
        setupLibrary();
        cast();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unrestricted search does not reveal and must put the card on top")
    void unrestrictedSearchIsHiddenAndPutsCardOnTop() {
        setupLibrary();
        cast();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().reveals()).isFalse();
        assertThat(search.params().canFailToFind()).isFalse();
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.TOP_OF_LIBRARY);
    }

    @Test
    @DisplayName("Empty library still causes the 2-life loss")
    void emptyLibraryStillLosesLife() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of());
        cast();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    private void cast() {
        harness.castFromHand(player1, new CruelTutor(), "{2}{B}");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new BogImp(), new GrizzlyBears(), new Island()));
    }
}
