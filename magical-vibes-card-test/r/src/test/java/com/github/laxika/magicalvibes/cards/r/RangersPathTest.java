package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.t.TempleGarden;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({RangersPath.class, Forest.class, Island.class, Plains.class, ElvishVisionary.class, TempleGarden.class})
class RangersPathTest extends BaseCardTest {

    @Test
    @DisplayName("Search offers only Forest cards and puts them onto the battlefield tapped")
    void searchOffersForestsToBattlefieldTapped() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .isNotEmpty()
                .allMatch(c -> c.getSubtypes().contains(CardSubtype.FOREST));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Fetches two Forests onto the battlefield tapped")
    void fetchesTwoForests() {
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
    @DisplayName("May find one Forest even when a second is available")
    void mayFindOnlyOneForest() {
        setupAndCast();
        setupLibrary();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .allSatisfy(p -> assertThat(p.isTapped()).isTrue());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Finishes with one tapped Forest when only one is available")
    void findsOnlyAvailableForest() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .allSatisfy(p -> assertThat(p.isTapped()).isTrue());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Resolves without a choice when the library contains no Forests")
    void noMatchingForests() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Island(), new ElvishVisionary()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolves with an empty library")
    void emptyLibrary() {
        setupAndCast();
        harness.setLibrary(player1, List.of());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Offers nonbasic Forest cards as well as basic Forests")
    void offersNonbasicForests() {
        setupAndCast();
        TempleGarden garden = new TempleGarden();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(new Island(), garden, forest));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = harness.getGameData().interaction
                .activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(garden, forest);
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new RangersPath()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Forest(), new Island(), new ElvishVisionary()));
    }
}
