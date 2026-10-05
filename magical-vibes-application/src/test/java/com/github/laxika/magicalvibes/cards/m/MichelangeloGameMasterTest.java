package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MichelangeloGameMaster.class, Forest.class})
class MichelangeloGameMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter at your end step after a permanent you controlled left")
    void getsCounterAfterPermanentLeaves() {
        Permanent michelangelo = harness.addToBattlefieldAndReturn(player1, new MichelangeloGameMaster());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, forest));

        resolveEndStepTrigger();

        assertThat(michelangelo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a counter without a permanent leaving under your control")
    void doesNotGetCounterWithoutPermanentLeaving() {
        Permanent michelangelo = harness.addToBattlefieldAndReturn(player1, new MichelangeloGameMaster());

        resolveEndStepTrigger();

        assertThat(michelangelo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not get a counter when only an opponent's permanent left")
    void doesNotGetCounterAfterOpponentsPermanentLeaves() {
        Permanent michelangelo = harness.addToBattlefieldAndReturn(player1, new MichelangeloGameMaster());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, forest));

        resolveEndStepTrigger();

        assertThat(michelangelo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void getsOnlyOneCounterWhenMultiplePermanentsLeave() {
        Permanent michelangelo = harness.addToBattlefieldAndReturn(player1, new MichelangeloGameMaster());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToHand(gd, first);
            harness.getPermanentRemovalService().removePermanentToHand(gd, second);
        });

        resolveEndStepTrigger();

        assertThat(michelangelo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void countsPermanentThatLeftBeforeMichelangeloEntered() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, forest));
        Permanent michelangelo = harness.addToBattlefieldAndReturn(player1, new MichelangeloGameMaster());

        resolveEndStepTrigger();

        assertThat(michelangelo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerAtOpponentsEndStep() {
        Permanent michelangelo = harness.addToBattlefieldAndReturn(player1, new MichelangeloGameMaster());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, forest));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(michelangelo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void leavingAfterEndStepBeginsDoesNotTriggerRetroactively() {
        Permanent michelangelo = harness.addToBattlefieldAndReturn(player1, new MichelangeloGameMaster());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, forest));

        assertThat(gd.stack).isEmpty();
        assertThat(michelangelo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void triggerDoesNotPutCounterOnSourceThatHasLeft() {
        Permanent michelangelo = harness.addToBattlefieldAndReturn(player1, new MichelangeloGameMaster());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, forest));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, michelangelo));
        harness.setHand(player1, List.of());
        Permanent returned = harness.addToBattlefieldAndReturn(player1, michelangelo.getCard());
        harness.passBothPriorities();

        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void permanentLeavingOnPreviousTurnDoesNotQualify() {
        Permanent michelangelo = harness.addToBattlefieldAndReturn(player1, new MichelangeloGameMaster());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, forest));

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(michelangelo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
