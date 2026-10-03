package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AcademyAtTolariaWest.class, Island.class})
class AcademyAtTolariaWestTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new AcademyAtTolariaWest(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
    }

    @Test
    void drawsSevenAtYourEndStepWithAnEmptyHand() {
        harness.setHand(player1, List.of());
        int before = gd.playerHands.get(player1.getId()).size();

        StepTriggerService steps = GameTestEngineContext.get().getBean(StepTriggerService.class);
        harness.inMutationScope(() -> steps.handleEndStepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(before + 7);
    }

    @Test
    void doesNotTriggerAtYourEndStepWithCardsInHand() {
        harness.setHand(player1, List.of(new Island()));

        StepTriggerService steps = GameTestEngineContext.get().getBean(StepTriggerService.class);
        harness.inMutationScope(() -> steps.handleEndStepTriggers(gd));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void chaosDiscardsYourEntireHand() {
        harness.setHand(player1, List.of(new Island(), new Island()));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void doesNotDrawIfHandIsNoLongerEmptyAtResolution() {
        harness.setHand(player1, List.of());
        StepTriggerService steps = GameTestEngineContext.get().getBean(StepTriggerService.class);
        harness.inMutationScope(() -> steps.handleEndStepTriggers(gd));
        assertThat(gd.stack).hasSize(1);
        Island drawnInResponse = new Island();
        harness.setHand(player1, List.of(drawnInResponse));
        int librarySize = gd.playerDecks.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnInResponse);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySize);
    }

    @Test
    void drawsForTheNewActivePlayerAtTheirEndStep() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of());
        Island otherPlayersCard = new Island();
        harness.setHand(player1, List.of(otherPlayersCard));
        StepTriggerService steps = GameTestEngineContext.get().getBean(StepTriggerService.class);

        harness.inMutationScope(() -> steps.handleEndStepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(otherPlayersCard);
    }

    @Test
    void chaosDiscardsOnlyItsControllersHandAtResolution() {
        Island opponentsCard = new Island();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(opponentsCard));
        harness.inMutationScope(() -> planar.chaos(gd));
        assertThat(gd.stack).hasSize(1);
        Island gainedInResponse = new Island();
        harness.setHand(player1, List.of(gainedInResponse));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(gainedInResponse);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opponentsCard);
    }

    @Test
    void endStepAbilityStillResolvesAfterLeavingThePlane() {
        harness.setHand(player1, List.of());
        StepTriggerService steps = GameTestEngineContext.get().getBean(StepTriggerService.class);
        harness.inMutationScope(() -> steps.handleEndStepTriggers(gd));
        assertThat(gd.stack).hasSize(1);
        gd.planechase.faceUp.clear();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
    }
}
