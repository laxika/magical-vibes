package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MichelangeloGameMaster.class, GrizzlyBears.class})
class MichelangeloGameMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter at your end step after a permanent you controlled left")
    void getsCounterAfterPermanentLeaves() {
        Permanent michelangelo = harness.addToBattlefieldAndReturn(player1, new MichelangeloGameMaster());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));

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
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));

        resolveEndStepTrigger();

        assertThat(michelangelo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
