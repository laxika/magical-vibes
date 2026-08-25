package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShamblingSwarm.class, Assassinate.class, HillGiant.class})
class ShamblingSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("On death, distributes three -1/-1 counters and removes them at the next end step")
    void distributesCountersAndRemovesThemAtNextEndStep() {
        Permanent swarm = addCreatureReady(player1, new ShamblingSwarm());
        Permanent first = addCreatureReady(player1, new HillGiant());
        Permanent second = addCreatureReady(player1, new HillGiant());

        killSwarm(swarm);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));
        harness.handleListChoice(player1, "2");
        harness.handleListChoice(player1, "1");
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.inMutationScope(() -> stepTriggerService().handleEndStepTriggers(gd));
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    private void killSwarm(Permanent swarm) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        swarm.tap();
        harness.setHand(player1, List.of(new Assassinate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID swarmId = swarm.getId();
        gs.playCard(gd, player1, 0, 0, swarmId, null);
        harness.passBothPriorities();
    }

    private StepTriggerService stepTriggerService() {
        return GameTestEngineContext.get().getBean(StepTriggerService.class);
    }
}
