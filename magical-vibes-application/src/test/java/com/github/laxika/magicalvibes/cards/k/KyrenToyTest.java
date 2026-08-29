package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KyrenToyTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} and tapping puts a charge counter on Kyren Toy")
    void firstAbilityAddsChargeCounter() {
        Permanent toy = addReadyToy(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(toy.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(toy.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing X charge counters adds X plus one colorless mana")
    void secondAbilityAddsOnePlusRemovedCounters() {
        Permanent toy = addReadyToy(player1);
        toy.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, 1, 2, null);

        assertThat(colorlessMana()).isEqualTo(3);
        assertThat(toy.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(toy.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing zero charge counters adds one colorless mana")
    void secondAbilityCanRemoveZeroCounters() {
        Permanent toy = addReadyToy(player1);
        toy.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 1, 0, null);

        assertThat(colorlessMana()).isEqualTo(1);
        assertThat(toy.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot remove more charge counters than Kyren Toy has")
    void secondAbilityRejectsTooManyCounters() {
        Permanent toy = addReadyToy(player1);
        toy.setCounterCount(CounterType.CHARGE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 2, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(toy.isTapped()).isFalse();
    }

    private Permanent addReadyToy(Player player) {
        Permanent permanent = new Permanent(new KyrenToy());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int colorlessMana() {
        return gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);
    }
}
