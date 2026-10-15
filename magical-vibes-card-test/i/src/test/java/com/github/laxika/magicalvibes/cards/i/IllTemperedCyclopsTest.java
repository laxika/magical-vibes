package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IllTemperedCyclops.class})
class IllTemperedCyclopsTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrosity puts three +1/+1 counters on Ill-Tempered Cyclops")
    void monstrosityAddsCountersAndMarksItMonstrous() {
        Permanent cyclops = addReadyCyclops();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(cyclops.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(cyclops.isMonstrous()).isTrue();
        assertThat(cyclops.getEffectivePower()).isEqualTo(6);
        assertThat(cyclops.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("A monstrous Cyclops can activate monstrosity again, but gets no more counters")
    void monstrousCyclopsCanActivateAgainWithoutAddingCounters() {
        Permanent cyclops = addReadyCyclops();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(cyclops.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(cyclops.isMonstrous()).isTrue();
    }

    @Test
    @DisplayName("Two stacked monstrosity activations put counters on Cyclops only once")
    void stackedMonstrosityActivationsAddCountersOnlyOnce() {
        Permanent cyclops = addReadyCyclops();
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(2);
        assertThat(cyclops.isMonstrous()).isFalse();
        assertThat(cyclops.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(cyclops.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(cyclops.isMonstrous()).isTrue();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Cyclops can activate monstrosity")
    void tappedSummoningSickCyclopsCanBecomeMonstrous() {
        Permanent cyclops = harness.addToBattlefieldAndReturn(player1, new IllTemperedCyclops());
        cyclops.setSummoningSick(true);
        cyclops.tap();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(cyclops.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(cyclops.isMonstrous()).isTrue();
    }

    private Permanent addReadyCyclops() {
        Permanent cyclops = harness.addToBattlefieldAndReturn(player1, new IllTemperedCyclops());
        cyclops.setSummoningSick(false);
        return cyclops;
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
