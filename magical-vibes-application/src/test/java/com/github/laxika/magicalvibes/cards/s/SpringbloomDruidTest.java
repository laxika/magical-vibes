package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TranquilThicket;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpringbloomDruid.class, SnowCoveredForest.class, SnowCoveredMountain.class, TranquilThicket.class})
class SpringbloomDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Entering may sacrifice a land to search for up to two tapped basic lands")
    void enteringSacrificesLandAndSearchesForBasicLands() {
        Permanent sacrificedLand = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        SnowCoveredForest forest = new SnowCoveredForest();
        SnowCoveredMountain mountain = new SnowCoveredMountain();
        SpringbloomDruid nonland = new SpringbloomDruid();
        harness.setLibrary(player1, List.of(forest, mountain, nonland));

        castSpringbloomDruid();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrificedLand.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificedLand.getCard());
        assertThat(findPermanent(forest).isTapped()).isTrue();
        assertThat(findPermanent(mountain).isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the entry ability does not sacrifice or search")
    void decliningEntryAbilityDoesNothing() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        SnowCoveredForest libraryForest = new SnowCoveredForest();
        harness.setLibrary(player1, List.of(libraryForest));

        castSpringbloomDruid();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .contains(land.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryForest);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(land.getCard());
    }

    @Test
    @DisplayName("Both searched lands are selected before either enters the battlefield")
    void searchedLandsEnterTogether() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        SnowCoveredForest forest = new SnowCoveredForest();
        SnowCoveredMountain mountain = new SnowCoveredMountain();
        harness.setLibrary(player1, List.of(forest, mountain));

        beginSearch(land);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .doesNotContain(forest, mountain);

        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(forest).isTapped()).isTrue();
        assertThat(findPermanent(mountain).isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search may find zero lands even when basic lands are available")
    void canFindZeroLands() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        SnowCoveredForest forest = new SnowCoveredForest();
        SnowCoveredMountain mountain = new SnowCoveredMountain();
        harness.setLibrary(player1, List.of(forest, mountain));

        beginSearch(land);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, mountain);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .doesNotContain(forest, mountain);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A nonbasic land can be sacrificed and the search may stop after one basic land")
    void canSacrificeNonbasicLandAndFindOneLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TranquilThicket());
        SnowCoveredForest forest = new SnowCoveredForest();
        SnowCoveredMountain mountain = new SnowCoveredMountain();
        harness.setLibrary(player1, List.of(forest, mountain));

        beginSearch(land);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(findPermanent(forest).isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mountain);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land.getCard());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Without a controlled land, accepting cannot search or sacrifice an opponent's land")
    void noControlledLandDoesNotSearch() {
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());
        SnowCoveredForest forest = new SnowCoveredForest();
        harness.setLibrary(player1, List.of(forest));

        castSpringbloomDruid();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing with an empty library still completes the ability")
    void emptyLibraryCompletesAbility() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        harness.setLibrary(player1, List.of());

        castSpringbloomDruid();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only one available basic land is found and nonbasic lands stay in the library")
    void onlyAvailableBasicLandIsFound() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        SnowCoveredForest forest = new SnowCoveredForest();
        TranquilThicket nonbasic = new TranquilThicket();
        SpringbloomDruid nonland = new SpringbloomDruid();
        harness.setLibrary(player1, List.of(nonbasic, forest, nonland));

        beginSearch(land);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(forest).isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(nonbasic, nonland);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land.getCard());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castSpringbloomDruid() {
        harness.castFromHand(player1, new SpringbloomDruid(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void beginSearch(Permanent land) {
        castSpringbloomDruid();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, land.getId());
        if (gd.interaction.activeInteraction() == null && !gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

    private Permanent findPermanent(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst()
                .orElseThrow();
    }
}
