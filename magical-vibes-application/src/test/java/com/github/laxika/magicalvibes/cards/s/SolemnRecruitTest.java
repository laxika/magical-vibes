package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AegisAutomaton;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SolemnRecruit.class, AegisAutomaton.class})
class SolemnRecruitTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter at your end step after a permanent you controlled left")
    void getsCounterAfterRevolt() {
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new SolemnRecruit());
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, automaton));

        resolveEndStepTrigger();

        assertThat(recruit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a counter without revolt")
    void doesNotGetCounterWithoutRevolt() {
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new SolemnRecruit());

        resolveEndStepTrigger();

        assertThat(recruit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not get a counter when only an opponent's permanent left")
    void doesNotGetCounterAfterOpponentsPermanentLeaves() {
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new SolemnRecruit());
        Permanent automaton = harness.addToBattlefieldAndReturn(player2, new AegisAutomaton());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, automaton));

        resolveEndStepTrigger();

        assertThat(recruit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Multiple departures give only one counter")
    void multipleDeparturesGiveOneCounter() {
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new SolemnRecruit());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first);
            harness.getPermanentRemovalService().removePermanentToExile(gd, second);
        });

        resolveEndStepTrigger();

        assertThat(recruit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Revolt counts a departure before Solemn Recruit entered")
    void countsDepartureBeforeEntering() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, automaton));
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new SolemnRecruit());

        resolveEndStepTrigger();

        assertThat(recruit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Revolt does not trigger during the opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new SolemnRecruit());
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, automaton));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(recruit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A departure after the end step begins cannot enable revolt retroactively")
    void departureAfterEndStepBeginsDoesNotTrigger() {
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new SolemnRecruit());
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, automaton));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(recruit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
