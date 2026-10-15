package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.k.KiorasFollower;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PheresBandTromper.class, KiorasFollower.class})
class PheresBandTromperTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping Pheres-Band Tromper puts a +1/+1 counter on it")
    void untappingPheresBandTromperAddsCounter() {
        Permanent tromper = harness.addToBattlefieldAndReturn(player1, new PheresBandTromper());
        tromper.setSummoningSick(false);
        tromper.tap();

        advanceToUpkeep();
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

        advanceToUpkeep();
        harness.passBothPriorities();

        assertThat(tromper.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isZero();
    }

    @Test
    void untappingOutsideUntapStepTriggersOnlyAfterAbilityResolves() {
        addCreatureReady(player1, new KiorasFollower());
        Permanent tromper = harness.addToBattlefieldAndReturn(player2, new PheresBandTromper());
        tromper.tap();

        harness.activateAbility(player1, 0, null, tromper.getId());
        harness.passBothPriorities();

        assertThat(tromper.isTapped()).isFalse();
        assertThat(tromper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(tromper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void untapEffectOnAlreadyUntappedTromperDoesNotTrigger() {
        addCreatureReady(player1, new KiorasFollower());
        Permanent tromper = harness.addToBattlefieldAndReturn(player1, new PheresBandTromper());

        harness.activateAbility(player1, 0, null, tromper.getId());
        harness.passBothPriorities();

        assertThat(tromper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eachUntappingTromperGetsItsOwnCounter() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PheresBandTromper());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PheresBandTromper());
        first.tap();
        second.tap();

        advanceToUpkeep();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void advanceToUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);
    }
}
