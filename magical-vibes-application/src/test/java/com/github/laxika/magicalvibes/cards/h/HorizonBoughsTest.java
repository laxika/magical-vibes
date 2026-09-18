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
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
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

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

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
}
