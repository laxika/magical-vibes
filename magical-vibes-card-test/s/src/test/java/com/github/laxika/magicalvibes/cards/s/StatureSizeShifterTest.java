package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StatureSizeShifter.class})
class StatureSizeShifterTest extends BaseCardTest {

    @Test
    @DisplayName("Stature can't be blocked while her power is 1 or less")
    void lowPowerStatureCantBeBlocked() {
        Permanent stature = addCreatureReady(player1, new StatureSizeShifter());

        assertThat(gqs.hasCantBeBlocked(gd, stature)).isTrue();

        stature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.getEffectivePower(gd, stature)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, stature)).isFalse();
    }

    @Test
    @DisplayName("Entry-turn power-up pays X and one blue mana")
    void powerUpIsDiscountedDuringEntryTurn() {
        Permanent stature = harness.enterBattlefieldAndReturn(player1, new StatureSizeShifter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(stature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power-up pays its full cost after the entry turn")
    void powerUpIsNotDiscountedAfterEntryTurn() {
        Permanent stature = addCreatureReady(player1, new StatureSizeShifter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(stature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power-up can be activated only once")
    void powerUpCanBeActivatedOnlyOnce() {
        addCreatureReady(player1, new StatureSizeShifter());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Choosing zero for X still uses Stature's power-up activation")
    void zeroXStillUsesPowerUp() {
        Permanent stature = harness.enterBattlefieldAndReturn(player1, new StatureSizeShifter());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(stature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasCantBeBlocked(gd, stature)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Power-up cannot be activated again while its first activation is on the stack")
    void powerUpCannotBeRepeatedBeforeResolution() {
        Permanent stature = addCreatureReady(player1, new StatureSizeShifter());
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null);

        assertThat(stature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");

        harness.passBothPriorities();

        assertThat(stature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, stature)).isFalse();
    }

    @Test
    @DisplayName("A single blue mana cannot pay for power-up after the entry turn")
    void entryDiscountDoesNotApplyToAnOlderStature() {
        Permanent stature = addCreatureReady(player1, new StatureSizeShifter());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(stature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A creature cannot actually block Stature at power one")
    void lowPowerStatureRejectsBlockers() {
        addCreatureReady(player1, new StatureSizeShifter());
        addCreatureReady(player2, new StatureSizeShifter());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
