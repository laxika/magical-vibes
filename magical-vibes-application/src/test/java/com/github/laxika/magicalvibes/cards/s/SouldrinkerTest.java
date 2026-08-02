package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SouldrinkerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 3 life puts a +1/+1 counter on Souldrinker")
    void payLifePutsCounter() {
        Permanent souldrinker = addCreatureReady(player1, new Souldrinker());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(souldrinker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(souldrinker.getEffectivePower()).isEqualTo(3);
        assertThat(souldrinker.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly, stacking counters")
    void abilityStacks() {
        Permanent souldrinker = addCreatureReady(player1, new Souldrinker());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        assertThat(souldrinker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate the ability with less than 3 life")
    void cannotActivateWithInsufficientLife() {
        addCreatureReady(player1, new Souldrinker());
        harness.setLife(player1, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }
}
