package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LongtuskCubTest extends BaseCardTest {

    @Test
    void gainsTwoEnergyWhenItDealsCombatDamageToAPlayer() {
        Permanent cub = addCreatureReady(player1, new LongtuskCub());
        cub.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void paysTwoEnergyToPutACounterOnItself() {
        Permanent cub = addCreatureReady(player1, new LongtuskCub());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(cub.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotActivateWithoutTwoEnergyCounters() {
        addCreatureReady(player1, new LongtuskCub());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two energy counters");
    }
}
