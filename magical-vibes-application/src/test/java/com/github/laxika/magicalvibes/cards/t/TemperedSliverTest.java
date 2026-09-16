package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GaleriderSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemperedSliver.class, GaleriderSliver.class, GrizzlyBears.class})
class TemperedSliverTest extends BaseCardTest {

    @Test
    void givesCombatDamageCounterAbilityToControlledSliversOnly() {
        Permanent temperedSliver = addCreatureReady(player1, new TemperedSliver());
        Permanent otherSliver = addCreatureReady(player1, new GaleriderSliver());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        temperedSliver.setAttacking(true);
        otherSliver.setAttacking(true);
        bear.setAttacking(true);

        resolveCombat(player1);
        harness.passBothPriorities();

        assertThat(temperedSliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherSliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
