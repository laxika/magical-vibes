package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SauroformHybridTest extends BaseCardTest {

    @Test
    void adaptPutsFourCountersOnCreature() {
        Permanent hybrid = addHybrid();
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hybrid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void adaptCanBeActivatedWithPlusOneCounter() {
        Permanent hybrid = addHybrid();
        hybrid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hybrid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void adaptChecksForCountersOnResolution() {
        Permanent hybrid = addHybrid();
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        hybrid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(hybrid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addHybrid() {
        Permanent hybrid = addCreatureReady(player1, new SauroformHybrid());
        hybrid.setSummoningSick(false);
        return hybrid;
    }

    private void addAdaptMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
