package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SauroformHybrid.class})
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

    @Test
    void adaptAddsCountersWhenExistingCountersAreRemovedBeforeResolution() {
        Permanent hybrid = addHybrid();
        hybrid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        hybrid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(hybrid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void adaptCanBeActivatedWhileTappedAndSummoningSick() {
        Permanent hybrid = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        hybrid.setSummoningSick(true);
        hybrid.tap();
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hybrid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(hybrid.isTapped()).isTrue();
    }

    private Permanent addHybrid() {
        return addCreatureReady(player1, new SauroformHybrid());
    }

    private void addAdaptMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
