package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FalseDawn;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlueManaBattery.class, FalseDawn.class})
class BlueManaBatteryTest extends BaseCardTest {

    // ===== Ability 0: {2}, {T}: Put a charge counter =====

    @Test
    @DisplayName("Paying {2} and tapping puts a charge counter on the battery")
    void firstAbilityAddsChargeCounter() {
        Permanent battery = harness.addToBattlefieldAndReturn(player1, new BlueManaBattery());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(battery.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(battery.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The charge-counter ability requires two generic mana")
    void firstAbilityRequiresTwoMana() {
        Permanent battery = harness.addToBattlefieldAndReturn(player1, new BlueManaBattery());
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
        Permanent battery = harness.addToBattlefieldAndReturn(player1, new BlueManaBattery());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        battery.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Ability 1: {T}, Remove any number of charge counters: Add {U} + one per removed =====

    @Test
    @DisplayName("Removing all charge counters adds the base {U} plus one per counter removed")
    void removingAllCountersAddsBasePlusPerCounter() {
        Permanent battery = harness.addToBattlefieldAndReturn(player1, new BlueManaBattery());
        battery.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "3");

        assertThat(blueMana()).isEqualTo(4); // 1 base + 3 removed
        assertThat(battery.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(battery.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing fewer counters than present keeps the rest and still adds the base {U}")
    void removingSomeCountersKeepsTheRest() {
        Permanent battery = harness.addToBattlefieldAndReturn(player1, new BlueManaBattery());
        battery.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "1");

        assertThat(blueMana()).isEqualTo(2); // 1 base + 1 removed
        assertThat(battery.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing zero counters still adds the base {U} and keeps every counter")
    void removingZeroCountersStillAddsBase() {
        Permanent battery = harness.addToBattlefieldAndReturn(player1, new BlueManaBattery());
        battery.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "0");

        assertThat(blueMana()).isEqualTo(1); // base only
        assertThat(battery.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(battery.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating with no charge counters adds the base {U} with no counter choice")
    void activatingWithNoCountersAddsBaseOnly() {
        Permanent battery = harness.addToBattlefieldAndReturn(player1, new BlueManaBattery());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(blueMana()).isEqualTo(1); // base only
        assertThat(battery.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot produce mana while the battery is already tapped")
    void secondAbilityRejectedWhenTapped() {
        Permanent battery = harness.addToBattlefieldAndReturn(player1, new BlueManaBattery());
        battery.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(blueMana()).isZero();
    }

    @Test
    @CardUsed(FalseDawn.class)
    @DisplayName("False Dawn replaces mana from both the battery and removed counters")
    void falseDawnReplacesAllManaFromBattery() {
        Permanent battery = harness.addToBattlefieldAndReturn(player1, new BlueManaBattery());
        battery.setCounterCount(CounterType.CHARGE, 2);
        harness.castFromHand(player1, new FalseDawn(), "{1}{W}");
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "2");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(3);
        assertThat(blueMana()).isZero();
    }

    @Test
    @DisplayName("Charging uses the stack and pays the mana and tap costs before resolution")
    void chargingWaitsForResolutionAfterPayingCosts() {
        Permanent battery = harness.addToBattlefieldAndReturn(player1, new BlueManaBattery());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(battery.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(battery.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(battery.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A second battery does not receive the first battery's charge counter")
    void chargeCounterGoesOnlyOnActivatedBattery() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BlueManaBattery());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BlueManaBattery());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(first.isTapped()).isFalse();
        assertThat(second.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The battery's controller receives its mana during the opponent's turn")
    void manaGoesToControllerDuringOpponentsTurn() {
        Permanent battery = harness.addToBattlefieldAndReturn(player2, new BlueManaBattery());
        battery.setCounterCount(CounterType.CHARGE, 2);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.handleListChoice(player2, "2");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(3);
        assertThat(blueMana()).isZero();
        assertThat(battery.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(battery.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private int blueMana() {
        return gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);
    }
}
