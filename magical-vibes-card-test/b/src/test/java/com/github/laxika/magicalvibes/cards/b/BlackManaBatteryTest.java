package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlackManaBattery.class})
class BlackManaBatteryTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2} and tapping puts a charge counter on the battery")
    void firstAbilityAddsChargeCounter() {
        Permanent battery = addCreatureReady(player1, new BlackManaBattery());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(battery.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(battery.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void firstAbilityRequiresTwoMana() {
        Permanent battery = addCreatureReady(player1, new BlackManaBattery());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(battery.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(battery.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot add a charge counter while the battery is already tapped")
    void firstAbilityRejectedWhenTapped() {
        Permanent battery = addCreatureReady(player1, new BlackManaBattery());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        battery.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Removing all charge counters adds the base {B} plus one per counter removed")
    void removingAllCountersAddsBasePlusPerCounter() {
        Permanent battery = addCreatureReady(player1, new BlackManaBattery());
        battery.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "3");

        assertThat(blackMana()).isEqualTo(4); // 1 base + 3 removed
        assertThat(battery.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(battery.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing fewer counters than present keeps the rest and still adds the base {B}")
    void removingSomeCountersKeepsTheRest() {
        Permanent battery = addCreatureReady(player1, new BlackManaBattery());
        battery.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "1");

        assertThat(blackMana()).isEqualTo(2); // 1 base + 1 removed
        assertThat(battery.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing zero counters still adds the base {B} and keeps every counter")
    void removingZeroCountersStillAddsBase() {
        Permanent battery = addCreatureReady(player1, new BlackManaBattery());
        battery.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "0");

        assertThat(blackMana()).isEqualTo(1); // base only
        assertThat(battery.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(battery.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating with no charge counters adds the base {B} with no counter choice")
    void activatingWithNoCountersAddsBaseOnly() {
        Permanent battery = addCreatureReady(player1, new BlackManaBattery());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(blackMana()).isEqualTo(1); // base only
        assertThat(battery.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot produce mana while the battery is already tapped")
    void secondAbilityRejectedWhenTapped() {
        Permanent battery = addCreatureReady(player1, new BlackManaBattery());
        battery.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(blackMana()).isZero();
    }

    @Test
    void chargingUsesTheStackAndPreservesExistingCounters() {
        Permanent battery = harness.addToBattlefieldAndReturn(player1, new BlackManaBattery());
        battery.setSummoningSick(true);
        battery.setCounterCount(CounterType.CHARGE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(battery.isTapped()).isTrue();
        assertThat(battery.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(battery.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void newlyEnteredNoncreatureBatteryProducesManaImmediately() {
        Permanent battery = harness.addToBattlefieldAndReturn(player1, new BlackManaBattery());
        battery.setSummoningSick(true);
        battery.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "2");

        assertThat(blackMana()).isEqualTo(3);
        assertThat(battery.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(battery.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void manaAbilityRemovesOnlyChargeCounters() {
        Permanent battery = addCreatureReady(player1, new BlackManaBattery());
        battery.setCounterCount(CounterType.CHARGE, 2);
        battery.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "2");

        assertThat(blackMana()).isEqualTo(3);
        assertThat(battery.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(battery.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private int blackMana() {
        return gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK);
    }
}
