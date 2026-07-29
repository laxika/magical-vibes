package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VentifactBottleTest extends BaseCardTest {

    @Test
    @DisplayName("{X}{1}, {T} puts X charge counters on the bottle")
    void activatedAbilityPutsXChargeCounters() {
        Permanent bottle = addBottle(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, 3, null);
        harness.passBothPriorities();

        assertThat(bottle.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(bottle.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The counter ability can only be activated as a sorcery")
    void activatedAbilityIsSorcerySpeedOnly() {
        addBottle(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.END_STEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 3, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("First main phase: taps the bottle, removes all charge counters and adds {C} for each")
    void mainPhaseTriggerTapsRemovesCountersAndAddsMana() {
        Permanent bottle = addBottle(player1);
        bottle.setCounterCount(CounterType.CHARGE, 3);

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(bottle.isTapped()).isTrue();
        assertThat(bottle.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Without a charge counter the ability does not even trigger")
    void doesNotTriggerWithoutChargeCounters() {
        Permanent bottle = addBottle(player1);

        advanceToPrecombatMain(player1);

        assertThat(gd.stack).isEmpty();

        harness.passBothPriorities();

        assertThat(bottle.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Does not trigger on an opponent's first main phase")
    void doesNotTriggerOnOpponentsTurn() {
        Permanent bottle = addBottle(player1);
        bottle.setCounterCount(CounterType.CHARGE, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(bottle.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    private Permanent addBottle(Player player) {
        return harness.addToBattlefieldAndReturn(player, new VentifactBottle());
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
