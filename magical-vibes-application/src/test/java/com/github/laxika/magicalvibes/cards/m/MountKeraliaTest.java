package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MountKeralia.class, GrizzlyBears.class, JaceBeleren.class})
class MountKeraliaTest extends BaseCardTest {

    private PlanechaseService planar;
    private PlanarObject source;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        source = new PlanarObject(new MountKeralia(), gd.nextTimestamp());
        gd.planechase.faceUp.add(source);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
    }

    @Test
    void controllerEndStepAddsPressureCounter() {
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleEndStepTriggers(gd));
        harness.passBothPriorities();

        assertThat(source.getCounters()).containsEntry(CounterType.PRESSURE, 1);
    }

    @Test
    void planeswalkingAwayDealsPressureDamageToCreaturesAndPlaneswalkers() {
        source.getCounters().put(CounterType.PRESSURE, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        planeswalkAway();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void chaosProtectsTheControllersPermanentsFromNamedPlaneDamageForTheGame() {
        source.getCounters().put(CounterType.PRESSURE, 2);
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent protectedPlaneswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        protectedPlaneswalker.setCounterCount(CounterType.LOYALTY, 3);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentPlaneswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        opponentPlaneswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        planeswalkAway();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(protectedCreature);
        assertThat(protectedPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(opponentPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    private void planeswalkAway() {
        harness.inMutationScope(() -> planar.planeswalk(gd));
        harness.passBothPriorities();
    }
}
