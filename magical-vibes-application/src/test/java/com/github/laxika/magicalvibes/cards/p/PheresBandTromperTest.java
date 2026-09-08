package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PheresBandTromperTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping Pheres-Band Tromper puts a +1/+1 counter on it")
    void untappingPheresBandTromperAddsCounter() {
        Permanent tromper = harness.addToBattlefieldAndReturn(player1, new PheresBandTromper());
        tromper.setSummoningSick(false);
        tromper.tap();

        advanceToUntapStep();
        harness.passBothPriorities();

        assertThat(tromper.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(1);
        assertThat(tromper.getEffectivePower()).isEqualTo(4);
        assertThat(tromper.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("An already untapped Pheres-Band Tromper does not trigger")
    void alreadyUntappedDoesNotTrigger() {
        Permanent tromper = harness.addToBattlefieldAndReturn(player1, new PheresBandTromper());
        tromper.setSummoningSick(false);

        advanceToUntapStep();
        harness.passBothPriorities();

        assertThat(tromper.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isZero();
    }

    private void advanceToUntapStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
