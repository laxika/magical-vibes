package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(KyrenToy.class)
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
    @DisplayName("First ability pays one generic mana")
    void firstAbilityPaysGenericMana() {
        addReadyToy(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(colorlessMana()).isEqualTo(1);
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
    @DisplayName("Choosing X removes that many charge counters and adds X plus one mana")
    void secondAbilityPromptsForXValue() {
        Permanent toy = addReadyToy(player1);
        toy.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, 1, null, null);

        PendingInteraction.XValueChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minValue()).isZero();
        assertThat(choice.maxValue()).isEqualTo(3);

        harness.handleXValueChosen(player1, 2);

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
    @DisplayName("Can remove zero charge counters when Kyren Toy has none")
    void secondAbilityCanRemoveZeroCountersWhenNoneAreAvailable() {
        Permanent toy = addReadyToy(player1);

        harness.activateAbility(player1, 0, 1, 0, null);

        assertThat(colorlessMana()).isEqualTo(1);
        assertThat(toy.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(toy.isTapped()).isTrue();
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
        return harness.addToBattlefieldAndReturn(player, new KyrenToy());
    }

    private int colorlessMana() {
        return gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);
    }
}
