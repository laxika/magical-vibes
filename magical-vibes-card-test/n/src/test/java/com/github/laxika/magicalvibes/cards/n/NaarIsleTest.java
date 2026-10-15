package com.github.laxika.magicalvibes.cards.n;

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

@CardUsed({NaarIsle.class, JaceBeleren.class})
class NaarIsleTest extends BaseCardTest {

    private PlanechaseService planar;
    private PlanarObject source;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        source = new PlanarObject(new NaarIsle(), gd.nextTimestamp());
        gd.planechase.faceUp.add(source);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
    }

    @Test
    void upkeepAddsFlameCounterThenDealsDamageEqualToTotalFlameCounters() {
        source.getCounters().put(CounterType.FLAME, 2);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(source.getCounters()).containsEntry(CounterType.FLAME, 3);
        harness.assertLife(player1, 17);
    }

    @Test
    void chaosDealsThreeDamageToTargetPlayer() {
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    void firstUpkeepDealsOneDamage() {
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(source.getCounters()).containsEntry(CounterType.FLAME, 1);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    void nextPlayersUpkeepKeepsCountersAndDamagesThatPlayer() {
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(source.getCounters()).containsEntry(CounterType.FLAME, 2);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);
    }

    @Test
    void upkeepCountsFlameCountersAtResolutionAndIgnoresOtherCounters() {
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        source.getCounters().put(CounterType.FLAME, 4);
        source.getCounters().put(CounterType.CHARGE, 7);
        harness.passBothPriorities();

        assertThat(source.getCounters()).containsEntry(CounterType.FLAME, 5)
                .containsEntry(CounterType.CHARGE, 7);
        harness.assertLife(player1, 15);
    }

    @Test
    void chaosCanTargetItsControllerAndDoesNotAddFlameCounters() {
        source.getCounters().put(CounterType.FLAME, 6);
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        assertThat(source.getCounters()).containsEntry(CounterType.FLAME, 6);
    }

    @Test
    @CardUsed(JaceBeleren.class)
    void chaosDealsThreeDamageToTargetPlaneswalker() {
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, jace.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Jace Beleren");
        harness.assertInGraveyard(player2, "Jace Beleren");
        harness.assertLife(player2, 20);
    }
}
