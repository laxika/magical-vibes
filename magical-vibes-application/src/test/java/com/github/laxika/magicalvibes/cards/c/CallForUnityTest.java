package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AegisAutomaton;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CallForUnity.class, AegisAutomaton.class, Opalescence.class})
class CallForUnityTest extends BaseCardTest {

    @Test
    @DisplayName("Adds a unity counter at your end step after a permanent you controlled left")
    void addsUnityCounterAfterRevolt() {
        Permanent callForUnity = harness.addToBattlefieldAndReturn(player1, new CallForUnity());
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, automaton));

        resolveEndStepTrigger();

        assertThat(callForUnity.getCounterCount(CounterType.UNITY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not add a unity counter without revolt")
    void doesNotAddUnityCounterWithoutRevolt() {
        Permanent callForUnity = harness.addToBattlefieldAndReturn(player1, new CallForUnity());

        resolveEndStepTrigger();

        assertThat(callForUnity.getCounterCount(CounterType.UNITY)).isZero();
    }

    @Test
    @DisplayName("Creatures you control get +1/+1 for each unity counter")
    void boostsOwnCreaturesForEachUnityCounter() {
        Permanent callForUnity = harness.addToBattlefieldAndReturn(player1, new CallForUnity());
        callForUnity.setCounterCount(CounterType.UNITY, 3);
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());

        var bonus = gqs.computeStaticBonus(gd, automaton);

        assertThat(bonus.power()).isEqualTo(3);
        assertThat(bonus.toughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost creatures controlled by an opponent")
    void doesNotBoostOpponentCreatures() {
        Permanent callForUnity = harness.addToBattlefieldAndReturn(player1, new CallForUnity());
        callForUnity.setCounterCount(CounterType.UNITY, 3);
        Permanent opponentAutomaton = harness.addToBattlefieldAndReturn(player2, new AegisAutomaton());

        var bonus = gqs.computeStaticBonus(gd, opponentAutomaton);

        assertThat(bonus.power()).isZero();
        assertThat(bonus.toughness()).isZero();
    }

    @Test
    void multipleDeparturesAddOnlyOneCounterAndUpdateTheBoost() {
        Permanent callForUnity = harness.addToBattlefieldAndReturn(player1, new CallForUnity());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        Permanent remaining = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToHand(gd, first);
            harness.getPermanentRemovalService().removePermanentToHand(gd, second);
        });

        resolveEndStepTrigger();

        assertThat(callForUnity.getCounterCount(CounterType.UNITY)).isEqualTo(1);
        var bonus = gqs.computeStaticBonus(gd, remaining);
        assertThat(bonus.power()).isEqualTo(1);
        assertThat(bonus.toughness()).isEqualTo(1);
    }

    @Test
    void opponentDepartureDoesNotEnableRevolt() {
        Permanent callForUnity = harness.addToBattlefieldAndReturn(player1, new CallForUnity());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AegisAutomaton());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, opponentCreature));

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(callForUnity.getCounterCount(CounterType.UNITY)).isZero();
    }

    @Test
    void departureAfterEndStepBeginsDoesNotTriggerRevolt() {
        Permanent callForUnity = harness.addToBattlefieldAndReturn(player1, new CallForUnity());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, creature));

        assertThat(gd.stack).isEmpty();
        assertThat(callForUnity.getCounterCount(CounterType.UNITY)).isZero();
    }

    @Test
    void doesNotTriggerDuringOpponentEndStep() {
        Permanent callForUnity = harness.addToBattlefieldAndReturn(player1, new CallForUnity());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, creature));

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(callForUnity.getCounterCount(CounterType.UNITY)).isZero();
    }

    @Test
    void boostEndsWhenCallForUnityLeavesTheBattlefield() {
        Permanent callForUnity = harness.addToBattlefieldAndReturn(player1, new CallForUnity());
        callForUnity.setCounterCount(CounterType.UNITY, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        assertThat(gqs.computeStaticBonus(gd, creature).power()).isEqualTo(2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, callForUnity));

        var bonus = gqs.computeStaticBonus(gd, creature);
        assertThat(bonus.power()).isZero();
        assertThat(bonus.toughness()).isZero();
    }

    @Test
    void departureBeforeCallForUnityEntersStillEnablesRevolt() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, creature));
        Permanent callForUnity = harness.enterBattlefieldAndReturn(player1, new CallForUnity());

        resolveEndStepTrigger();

        assertThat(callForUnity.getCounterCount(CounterType.UNITY)).isEqualTo(1);
    }

    @Test
    void departureInPreviousTurnDoesNotEnableRevolt() {
        Permanent callForUnity = harness.addToBattlefieldAndReturn(player1, new CallForUnity());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, creature));

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(callForUnity.getCounterCount(CounterType.UNITY)).isZero();
    }

    @Test
    void boostsFromMultipleCopiesAddTogether() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CallForUnity());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CallForUnity());
        first.setCounterCount(CounterType.UNITY, 2);
        second.setCounterCount(CounterType.UNITY, 3);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());

        var bonus = gqs.computeStaticBonus(gd, creature);

        assertThat(bonus.power()).isEqualTo(5);
        assertThat(bonus.toughness()).isEqualTo(5);
    }

    @Test
    void boostsItselfWhenItBecomesACreature() {
        Permanent callForUnity = harness.addToBattlefieldAndReturn(player1, new CallForUnity());
        callForUnity.setCounterCount(CounterType.UNITY, 3);
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, callForUnity)).isTrue();
        assertThat(gqs.getEffectivePower(gd, callForUnity)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, callForUnity)).isEqualTo(8);
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
