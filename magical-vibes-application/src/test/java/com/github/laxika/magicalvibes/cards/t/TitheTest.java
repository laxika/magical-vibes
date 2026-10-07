package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Savannah;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Tithe.class, Forest.class, Plains.class, GrizzlyBears.class, Savannah.class})
class TitheTest extends BaseCardTest {

    @Test
    @DisplayName("When opponent has more lands, search allows up to two Plains")
    void opponentHasMoreLandsAllowsTwoPlains() {
        setupAndCast();
        harness.addToBattlefield(player2, new Forest());
        List<Card> library = setupLibrary(2);

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().cards()).containsExactly(library.get(0), library.get(1));
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(library.get(0), library.get(1));
    }

    @Test
    @DisplayName("When opponent has more lands, the additional Plains search may be declined")
    void mayDeclineAdditionalPlains() {
        setupAndCast();
        harness.addToBattlefield(player2, new Forest());
        List<Card> library = setupLibrary(2);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        PendingInteraction.LibrarySearch additionalSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(additionalSearch).isNotNull();
        assertThat(additionalSearch.params().remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(0));
    }

    @Test
    @DisplayName("When land counts are equal, search allows only one Plains")
    void equalLandsAllowsOnlyOnePlains() {
        setupAndCast();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        List<Card> library = setupLibrary(2);

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(0));
    }

    @Test
    @DisplayName("Land count is checked on resolution, not announcement")
    void landCountCheckedOnResolution() {
        setupAndCast();
        setupLibrary(2);

        // Equal at announcement; opponent gains a land before resolution.
        harness.addToBattlefield(player2, new Forest());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().remainingCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new Tithe()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("An opponent's initial land advantage can disappear before resolution")
    void landAdvantageLostBeforeResolution() {
        harness.addToBattlefield(player2, new Forest());
        setupAndCast();
        List<Card> library = setupLibrary(2);
        harness.addToBattlefield(player1, new Forest());

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(0));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(library.get(1), library.get(2));
    }

    @Test
    @DisplayName("A restricted search may find no Plains even when Plains are present")
    void mayFailToFindAllPlains() {
        setupAndCast();
        harness.addToBattlefield(player2, new Forest());
        List<Card> library = setupLibrary(2);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        harness.assertInGraveyard(player1, "Tithe");
    }

    @Test
    @DisplayName("When you control more lands, only one Plains can be found")
    void controllerHasMoreLandsAllowsOnlyOnePlains() {
        setupAndCast();
        harness.addToBattlefield(player1, new Forest());
        List<Card> library = setupLibrary(2);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(0));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(library.get(1), library.get(2));
    }

    @Test
    @DisplayName("A nonbasic Plains can be found, but a Forest cannot")
    void canFindNonbasicPlains() {
        setupAndCast();
        Savannah savannah = new Savannah();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, savannah));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(savannah);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(savannah);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Finding the only Plains completes the search even when two are allowed")
    void onlyOneMatchingPlainsCompletesSearch() {
        setupAndCast();
        harness.addToBattlefield(player2, new Forest());
        List<Card> library = setupLibrary(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(0));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(1));
    }

    @Test
    @DisplayName("A library with no Plains resolves without moving any cards")
    void noMatchingPlains() {
        setupAndCast();
        List<Card> library = setupLibrary(0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        harness.assertInGraveyard(player1, "Tithe");
    }

    @Test
    @DisplayName("An empty library does not prevent Tithe from resolving")
    void emptyLibrary() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Tithe");
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new Tithe()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, player2.getId());
    }

    private List<Card> setupLibrary(int plainsCount) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < plainsCount; i++) {
            deck.add(new Plains());
        }
        deck.add(new GrizzlyBears());
        harness.setLibrary(player1, deck);
        return deck;
    }
}
