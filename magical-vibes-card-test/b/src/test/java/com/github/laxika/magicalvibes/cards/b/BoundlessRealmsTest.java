package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoundlessRealms.class, Forest.class, Plains.class, Island.class, GrizzlyBears.class, EvolvingWilds.class})
class BoundlessRealmsTest extends BaseCardTest {

    @Test
    @DisplayName("Search offers only basic lands and puts them onto the battlefield tapped")
    void searchOffersBasicLandsToBattlefieldTapped() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Fetches as many basic lands as the controller has lands")
    void fetchesOneLandPerControlledLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND) && p.isTapped())).hasSize(2);
    }

    @Test
    @DisplayName("Controller may stop searching early")
    void mayFailToFindEarly() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
    }

    @Test
    @DisplayName("Controlling no lands searches for nothing")
    void noLandsNoSearch() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
    }

    @Test
    @DisplayName("Selected lands wait until the search finishes before entering together")
    void landsEnterTogetherAfterSelection() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        setupAndCast();
        setupLibrary();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).hasSize(2);
        harness.handleCardChosen(player1, 0);
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).hasSize(4);
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.isTapped())).hasSize(2);
    }

    @Test
    @DisplayName("Nonbasic lands count toward X but cannot be found, and opposing lands do not count")
    void countsOnlyControllersLandsAndFindsOnlyBasics() {
        harness.addToBattlefield(player1, new EvolvingWilds());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        setupAndCast();
        EvolvingWilds nonbasic = new EvolvingWilds();
        Plains basic = new Plains();
        Forest otherBasic = new Forest();
        harness.setLibrary(player1, List.of(nonbasic, basic, otherBasic));
        harness.passBothPriorities();

        var search = harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(basic, otherBasic);
        harness.handleCardChosen(player1, 0);

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(harness.getGameData().playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(nonbasic, otherBasic);
    }

    @Test
    @DisplayName("Controller may find one land and decline the rest")
    void mayStopAfterOneLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        setupAndCast();
        setupLibrary();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.isTapped())).hasSize(1);
    }

    @Test
    @DisplayName("Land count is determined on resolution rather than on casting")
    void countsLandsAtResolution() {
        harness.addToBattlefield(player1, new Forest());
        setupAndCast();
        setupLibrary();
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Search finishes when fewer basic lands remain than the permitted count")
    void fewerBasicsThanControlledLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        setupAndCast();
        Plains basic = new Plains();
        EvolvingWilds nonbasic = new EvolvingWilds();
        harness.setLibrary(player1, List.of(basic, nonbasic));
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.isTapped())).hasSize(1);
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).containsExactly(nonbasic);
    }

    @Test
    @DisplayName("A library with no basic lands produces no selection or new permanents")
    void noMatchingBasicLands() {
        harness.addToBattlefield(player1, new Forest());
        setupAndCast();
        EvolvingWilds nonbasic = new EvolvingWilds();
        harness.setLibrary(player1, List.of(nonbasic));
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).containsExactly(nonbasic);
    }

    @Test
    @DisplayName("An empty library resolves without requesting a selection")
    void emptyLibrary() {
        harness.addToBattlefield(player1, new Forest());
        setupAndCast();
        harness.setLibrary(player1, List.of());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).isEmpty();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new BoundlessRealms()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }
}
