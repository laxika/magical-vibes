package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CallForUnityTest extends BaseCardTest {

    @Test
    @DisplayName("Adds a unity counter at your end step after a permanent you controlled left")
    void addsUnityCounterAfterRevolt() {
        Permanent callForUnity = harness.addToBattlefieldAndReturn(player1, new CallForUnity());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));

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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        var bonus = gqs.computeStaticBonus(gd, bears);

        assertThat(bonus.power()).isEqualTo(3);
        assertThat(bonus.toughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost creatures controlled by an opponent")
    void doesNotBoostOpponentCreatures() {
        Permanent callForUnity = harness.addToBattlefieldAndReturn(player1, new CallForUnity());
        callForUnity.setCounterCount(CounterType.UNITY, 3);
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        var bonus = gqs.computeStaticBonus(gd, opponentBears);

        assertThat(bonus.power()).isZero();
        assertThat(bonus.toughness()).isZero();
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
