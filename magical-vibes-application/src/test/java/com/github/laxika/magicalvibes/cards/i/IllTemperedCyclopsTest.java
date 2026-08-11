package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    @DisplayName("Ill-Tempered Cyclops's monstrosity ability can resolve only once")
    void monstrosityOnlyResolvesOnce() {
        addReadyCyclops();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addMonstrosityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already monstrous");
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
