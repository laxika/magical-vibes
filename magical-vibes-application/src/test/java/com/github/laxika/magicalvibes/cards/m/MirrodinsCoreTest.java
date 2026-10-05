package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(MirrodinsCore.class)
class MirrodinsCoreTest extends BaseCardTest {

    @Test
    @DisplayName("First ability taps for one colorless mana")
    void tapsForColorlessMana() {
        Permanent core = addReadyCore(0);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(core.isTapped()).isTrue();
        assertThat(colorlessMana()).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability taps and puts a charge counter on the land")
    void storesChargeCounter() {
        Permanent core = addReadyCore(0);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(core.isTapped()).isTrue();
        assertThat(core.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Third ability removes a charge counter and adds the chosen color")
    void convertsChargeCounterToChosenColorMana() {
        Permanent core = addReadyCore(1);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(core.isTapped()).isTrue();
        assertThat(core.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Third ability cannot be activated without a charge counter")
    void cannotConvertWithoutChargeCounter() {
        addReadyCore(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void convertsExactlyOneCounterToAnyColorWithoutUsingTheStack(ManaColor color) {
        Permanent core = addReadyCore(3);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(core.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(colorlessMana()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void storingChargeUsesTheStackAndAddsToExistingCounters() {
        Permanent core = addReadyCore(2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(core.isTapped()).isTrue();
        assertThat(core.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        assertThat(colorlessMana()).isZero();

        harness.passBothPriorities();

        assertThat(core.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void failedCounterPaymentDoesNotTapTheLand() {
        Permanent core = addReadyCore(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(core.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(colorlessMana()).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void tappedLandCannotActivateAnyAbility(int abilityIndex) {
        Permanent core = addReadyCore(1);
        core.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(core.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void newlyEnteredNoncreatureLandCanTapForMana() {
        Permanent core = harness.addToBattlefieldAndReturn(player1, new MirrodinsCore());
        core.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(core.isTapped()).isTrue();
        assertThat(colorlessMana()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyCore(int chargeCounters) {
        Permanent core = harness.addToBattlefieldAndReturn(player1, new MirrodinsCore());
        core.setSummoningSick(false);
        core.setCounterCount(CounterType.CHARGE, chargeCounters);
        return core;
    }

    private int colorlessMana() {
        return gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);
    }
}
