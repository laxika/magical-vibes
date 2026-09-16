package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CarrionWurm;
import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShamblingSwarm.class, FieryTemper.class, CarrionWurm.class})
class ShamblingSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Can put all three -1/-1 counters on one target creature")
    void putsAllCountersOnOneTargetCreature() {
        Permanent swarm = addCreatureReady(player1, new ShamblingSwarm());
        Permanent target = addCreatureReady(player1, new CarrionWurm());

        killSwarm(swarm);
        harness.handlePermanentChosen(player1, target.getId());
        harness.handleListChoice(player1, "3");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can put one -1/-1 counter on each of three target creatures")
    void distributesCountersAcrossThreeTargets() {
        Permanent swarm = addCreatureReady(player1, new ShamblingSwarm());
        Permanent first = addCreatureReady(player1, new CarrionWurm());
        Permanent second = addCreatureReady(player1, new CarrionWurm());
        Permanent third = addCreatureReady(player1, new CarrionWurm());

        killSwarm(swarm);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, third.getId());
        harness.handleListChoice(player1, "1");
        harness.handleListChoice(player1, "1");
        harness.handleListChoice(player1, "1");
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isOne();
        assertThat(second.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isOne();
        assertThat(third.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isOne();
    }

    @Test
    @DisplayName("On death, distributes three -1/-1 counters and removes them at the next end step")
    void distributesCountersAndRemovesThemAtNextEndStep() {
        Permanent swarm = addCreatureReady(player1, new ShamblingSwarm());
        Permanent first = addCreatureReady(player1, new CarrionWurm());
        Permanent second = addCreatureReady(player1, new CarrionWurm());

        killSwarm(swarm);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handleListChoice(player1, "2");
        harness.handleListChoice(player1, "1");
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        resolveNextEndStepCounterRemovals();

        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The next end step removes only the counters put on each creature by this trigger")
    void onlyCountersPutByThisTriggerAreRemoved() {
        Permanent swarm = addCreatureReady(player1, new ShamblingSwarm());
        Permanent first = addCreatureReady(player1, new CarrionWurm());
        Permanent second = addCreatureReady(player1, new CarrionWurm());
        first.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        killSwarm(swarm);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handleListChoice(player1, "1");
        harness.handleListChoice(player1, "2");
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);

        resolveNextEndStepCounterRemovals();

        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isOne();
        assertThat(second.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    private void killSwarm(Permanent swarm) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FieryTemper()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, swarm.getId());
        harness.passBothPriorities();
    }

    private void resolveNextEndStepCounterRemovals() {
        harness.forceStep(TurnStep.END_STEP);
        harness.inMutationScope(() -> stepTriggerService().handleEndStepTriggers(gd));
        harness.clearPriorityPassed();
        resolveAllTriggers();
    }

    private StepTriggerService stepTriggerService() {
        return GameTestEngineContext.get().getBean(StepTriggerService.class);
    }
}
