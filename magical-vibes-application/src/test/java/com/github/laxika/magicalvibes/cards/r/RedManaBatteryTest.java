package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RedManaBattery.class})
class RedManaBatteryTest extends BaseCardTest {

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
    @DisplayName("The charge-counter ability requires two generic mana")
    void firstAbilityRequiresTwoMana() {
        Permanent battery = addReadyBattery(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(battery.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(battery.isTapped()).isFalse();
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

    @Test
    @DisplayName("Cannot produce mana while the battery is already tapped")
    void secondAbilityRejectedWhenTapped() {
        Permanent battery = addReadyBattery(player1);
        battery.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(redMana()).isZero();
    }

    @Test
    @DisplayName("Adding a charge counter uses the stack and pays its costs immediately")
    void chargeCounterIsAddedOnlyOnResolution() {
        Permanent battery = addReadyBattery(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(battery.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(battery.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(battery.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The mana ability removes only charge counters and resolves without the stack")
    void manaAbilityLeavesOtherCountersAlone() {
        Permanent battery = addReadyBattery(player1);
        battery.setCounterCount(CounterType.CHARGE, 2);
        battery.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "2");

        assertThat(redMana()).isEqualTo(3);
        assertThat(battery.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(battery.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A battery controlled by the other player adds mana to that player's pool")
    void manaGoesToTheActivatingController() {
        Permanent battery = addReadyBattery(player2);
        battery.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.handleListChoice(player2, "2");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(redMana()).isZero();
        assertThat(battery.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(battery.isTapped()).isTrue();
    }

    private Permanent addReadyBattery(Player player) {
        return harness.addToBattlefieldAndReturn(player, new RedManaBattery());
    }

    private int redMana() {
        return gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);
    }
}
