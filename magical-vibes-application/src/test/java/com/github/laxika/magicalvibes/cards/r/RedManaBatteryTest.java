package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RedManaBatteryTest extends BaseCardTest {

    // ===== Ability 0: {2}, {T}: Put a charge counter =====

    @Test
    @DisplayName("Paying {2} and tapping puts a charge counter on the battery")
    void firstAbilityAddsChargeCounter() {
        Permanent battery = addReadyBattery(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(battery.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(battery.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot add a charge counter while the battery is already tapped")
    void firstAbilityRejectedWhenTapped() {
        Permanent battery = addReadyBattery(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        battery.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Ability 1: {T}, Remove any number of charge counters: Add {R} + one per removed =====

    @Test
    @DisplayName("Removing all charge counters adds the base {R} plus one per counter removed")
    void removingAllCountersAddsBasePlusPerCounter() {
        Permanent battery = addReadyBattery(player1);
        battery.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "3");

        assertThat(redMana()).isEqualTo(4); // 1 base + 3 removed
        assertThat(battery.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(battery.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing fewer counters than present keeps the rest and still adds the base {R}")
    void removingSomeCountersKeepsTheRest() {
        Permanent battery = addReadyBattery(player1);
        battery.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "1");

        assertThat(redMana()).isEqualTo(2); // 1 base + 1 removed
        assertThat(battery.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing zero counters still adds the base {R} and keeps every counter")
    void removingZeroCountersStillAddsBase() {
        Permanent battery = addReadyBattery(player1);
        battery.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "0");

        assertThat(redMana()).isEqualTo(1); // base only
        assertThat(battery.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(battery.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating with no charge counters adds the base {R} with no counter choice")
    void activatingWithNoCountersAddsBaseOnly() {
        Permanent battery = addReadyBattery(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(redMana()).isEqualTo(1); // base only
        assertThat(battery.isTapped()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addReadyBattery(Player player) {
        Permanent perm = new Permanent(new RedManaBattery());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int redMana() {
        return gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);
    }
}
