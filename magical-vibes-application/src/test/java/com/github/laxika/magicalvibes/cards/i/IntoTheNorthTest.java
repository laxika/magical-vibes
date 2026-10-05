package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScryingSheets;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IntoTheNorth.class, Forest.class, GrizzlyBears.class, SnowCoveredForest.class,
        SnowCoveredIsland.class, ScryingSheets.class, BorealDruid.class})
class IntoTheNorthTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers only snow lands for the tapped battlefield search")
    void resolvingOffersOnlySnowLands() {
        SnowCoveredForest snowForest = new SnowCoveredForest();
        SnowCoveredIsland snowIsland = new SnowCoveredIsland();
        setupAndCast(List.of(snowForest, new Forest(), snowIsland, new GrizzlyBears()));

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards()).containsExactly(snowForest, snowIsland);
    }

    @Test
    @DisplayName("Choosing a snow land puts it onto the battlefield tapped")
    void chosenSnowLandEntersTapped() {
        SnowCoveredForest snowForest = new SnowCoveredForest();
        setupAndCast(List.of(snowForest));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == snowForest && permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search may fail to find a snow land")
    void canFailToFind() {
        setupAndCast(List.of(new Forest(), new GrizzlyBears()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A nonbasic snow land can be found and enters tapped")
    void canFindNonbasicSnowLand() {
        ScryingSheets sheets = new ScryingSheets();
        BorealDruid druid = new BorealDruid();
        setupAndCast(List.of(druid, sheets));

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(sheets);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(sheets);
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(druid);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search may fail to find even when a snow land is available")
    void canDeclineAvailableSnowLand() {
        SnowCoveredForest snowForest = new SnowCoveredForest();
        setupAndCast(List.of(snowForest));

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(snowForest);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Into the North");
    }

    @Test
    @DisplayName("An empty library finishes the search without a choice")
    void emptyLibraryFinishesSearch() {
        setupAndCast(List.of());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Into the North");
    }

    private void setupAndCast(List<Card> library) {
        harness.setHand(player1, List.of(new IntoTheNorth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, library);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
