package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Forest.class, GrizzlyBears.class, HorizonBoughs.class, Island.class, Mountain.class})
class HorizonBoughsTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new HorizonBoughs(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("All permanents untap during each player's untap step")
    void allPermanentsUntapDuringEachPlayersUntapStep() {
        Permanent playerOnePermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent playerTwoPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        playerOnePermanent.tap();
        playerTwoPermanent.tap();

        harness.performUntapStep(player1);

        assertThat(playerOnePermanent.isTapped()).isFalse();
        assertThat(playerTwoPermanent.isTapped()).isFalse();

        playerOnePermanent.tap();
        playerTwoPermanent.tap();
        harness.performUntapStep(player2);

        assertThat(playerOnePermanent.isTapped()).isFalse();
        assertThat(playerTwoPermanent.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Chaos may search for up to three basic lands and puts them onto the battlefield tapped")
    void chaosSearchesForUpToThreeBasicLandsTapped() {
        Card forest = new Forest();
        Card island = new Island();
        Card mountain = new Mountain();
        Card nonBasicLand = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, island, mountain, nonBasicLand));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().remainingCount()).isEqualTo(3);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(forest, island, mountain);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isTapped)
                .extracting(permanent -> permanent.getCard())
                .containsExactlyInAnyOrder(forest, island, mountain);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonBasicLand);
    }

    @Test
    @DisplayName("Declining the chaos search does not move cards")
    void decliningChaosSearchDoesNothing() {
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard())
                .doesNotContain(forest);
    }

    @Test
    @DisplayName("The chaos search may stop after finding one land")
    void chaosSearchMayStopAfterOneLand() {
        Card forest = new Forest();
        Card island = new Island();
        harness.setLibrary(player1, List.of(forest, island));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isTapped)
                .extracting(Permanent::getCard)
                .containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Accepting the chaos search permits finding zero lands")
    void chaosSearchMayFindZeroLands() {
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Chaos resolves when the library has no basic lands")
    void chaosSearchWithNoBasicLandsFinishes() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The additional untaps stop after planeswalking away")
    void leavingPlaneStopsOtherPlayersUntaps() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Island());
        ownLand.tap();
        opposingLand.tap();
        gd.planechase.faceUp.clear();

        harness.performUntapStep(player1);

        assertThat(ownLand.isTapped()).isFalse();
        assertThat(opposingLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Chaos finds at most three lands even when more are available")
    void chaosSearchCannotFindMoreThanThreeLands() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isTapped)
                .extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Lands also untap during the opposing player's untap step")
    void landsUntapDuringOtherPlayersUntapStep() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Island());
        ownLand.tap();
        opposingLand.tap();

        harness.performUntapStep(player1);

        assertThat(ownLand.isTapped()).isFalse();
        assertThat(opposingLand.isTapped()).isFalse();
    }
}
