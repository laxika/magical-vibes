package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.Anaconda;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GaeasBounty.class, Forest.class, Island.class, Anaconda.class})
class GaeasBountyTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving presents only Forest cards")
    void searchShowsOnlyForests() {
        setupAndCast();
        List<Card> library = setupLibrary();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(library.get(0), library.get(1));
        assertThat(search.params().reveals()).isTrue();
    }

    @Test
    @DisplayName("Up to two Forest cards can be put into hand")
    void putsTwoForestsIntoHand() {
        setupAndCast();
        List<Card> library = setupLibrary();

        harness.passBothPriorities();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).contains(library.get(0), library.get(1));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Up to two Forest cards allows stopping after one")
    void mayChooseOnlyOneForest() {
        setupAndCast();
        List<Card> library = setupLibrary();

        harness.passBothPriorities();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(library.get(0));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(library.get(1), library.get(2), library.get(3));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("A search with no Forest cards does not prompt")
    void noForestsNoPrompt() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Island(), new Anaconda()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("May choose zero Forests even when two are available")
    void mayChooseZeroForests() {
        setupAndCast();
        List<Card> library = setupLibrary();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertInGraveyard(player1, "Gaea's Bounty");
    }

    @Test
    @DisplayName("Finding the only Forest completes the search")
    void onlyOneForestAvailable() {
        setupAndCast();
        Forest forest = new Forest();
        Island island = new Island();
        harness.setLibrary(player1, List.of(forest, island));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertInGraveyard(player1, "Gaea's Bounty");
    }

    @Test
    @DisplayName("Search stops at two Forests even when more are available")
    void cannotTakeMoreThanTwoForests() {
        setupAndCast();
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2).contains(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new GaeasBounty(), "{2}{G}");
    }

    private List<Card> setupLibrary() {
        List<Card> library = List.of(new Forest(), new Forest(), new Island(), new Anaconda());
        harness.setLibrary(player1, library);
        return library;
    }
}
