package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NissasRenewal.class, Forest.class, Island.class, Mountain.class, GrizzlyBears.class, EvolvingWilds.class})
class NissasRenewalTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for up to three basic lands and puts them onto the battlefield tapped")
    void searchesForThreeBasicLandsTapped() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain(), new GrizzlyBears()));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(3);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(3)
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Gains 7 life after the library search")
    void gainsSevenLifeAfterSearch() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 10);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Choosing no lands still gains life and leaves the library intact")
    void canChooseNoLands() {
        setupAndCast();
        Forest forest = new Forest();
        Island island = new Island();
        harness.setLibrary(player1, List.of(forest, island));
        harness.setLife(player1, 10);
        harness.setLife(player2, 12);

        harness.passBothPriorities();
        harness.assertLife(player1, 10);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, island);
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 12);
        harness.assertInGraveyard(player1, "Nissa's Renewal");
    }

    @Test
    @DisplayName("Stopping after one land puts it onto the battlefield tapped and gains life")
    void canStopAfterOneLand() {
        setupAndCast();
        Forest forest = new Forest();
        Island island = new Island();
        harness.setLibrary(player1, List.of(forest, island));
        harness.setLife(player1, 10);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertLife(player1, 10);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(1).allMatch(Permanent::isTapped)
                .extracting(Permanent::getCard).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("A library with only two matching lands finishes the search and gains life")
    void finishesWhenMatchingLandsRunOut() {
        setupAndCast();
        EvolvingWilds nonbasic = new EvolvingWilds();
        harness.setLibrary(player1, List.of(new Forest(), new Island(), nonbasic));
        harness.setLife(player1, 10);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).hasSize(2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(2).allMatch(Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonbasic);
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("The search permits repeated basic land names but stops at three")
    void findsAtMostThreeLandsWithTheSameName() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLife(player1, 10);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(3).allMatch(Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("An empty library does not prevent gaining seven life")
    void gainsLifeWithEmptyLibrary() {
        setupAndCast();
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 10);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 17);
        harness.assertInGraveyard(player1, "Nissa's Renewal");
    }
    private void setupAndCast() {
        harness.setHand(player1, List.of(new NissasRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castSorcery(player1, 0, 0);
    }
}
