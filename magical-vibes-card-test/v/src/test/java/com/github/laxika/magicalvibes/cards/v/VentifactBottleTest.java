package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VentifactBottle.class})
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
    @DisplayName("The trigger does not fire during the postcombat main phase")
    void doesNotTriggerDuringPostcombatMainPhase() {
        Permanent bottle = addBottle(player1);
        bottle.setCounterCount(CounterType.CHARGE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(bottle.isTapped()).isFalse();
        assertThat(bottle.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Removing the charge counters before resolution prevents the trigger")
    void triggerDoesNothingIfCountersAreRemovedBeforeResolution() {
        Permanent bottle = addBottle(player1);
        bottle.setCounterCount(CounterType.CHARGE, 3);

        advanceToPrecombatMain(player1);
        assertThat(gd.stack).hasSize(1);

        bottle.setCounterCount(CounterType.CHARGE, 0);
        harness.passBothPriorities();

        assertThat(bottle.isTapped()).isFalse();
        assertThat(bottle.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
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

        advanceToPrecombatMain(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(bottle.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("X may be zero, but the generic mana and tap costs must still be paid")
    void zeroXStillCostsOneManaAndTapsBottle() {
        Permanent bottle = addBottle(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, 0, null);
        assertThat(bottle.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();

        assertThat(bottle.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("The counter ability cannot be activated while another ability is on the stack")
    void cannotActivateWithNonemptyStack() {
        addBottle(player1);
        Permanent second = addBottle(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, 1, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(second.isTapped()).isFalse();
        assertThat(second.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("An already tapped bottle still removes charge counters and produces mana")
    void tappedBottleStillProducesMana() {
        Permanent bottle = addBottle(player1);
        bottle.tap();
        bottle.setCounterCount(CounterType.CHARGE, 2);

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(bottle.isTapped()).isTrue();
        assertThat(bottle.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("The trigger counts charge counters at resolution and leaves other counters intact")
    void countsChargeCountersAtResolution() {
        Permanent bottle = addBottle(player1);
        bottle.setCounterCount(CounterType.CHARGE, 2);
        bottle.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        advanceToPrecombatMain(player1);
        assertThat(gd.stack).hasSize(1);
        bottle.setCounterCount(CounterType.CHARGE, 4);

        harness.passBothPriorities();

        assertThat(bottle.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(bottle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
    }

    @Test
    @DisplayName("Each bottle removes its own counters and awards its own amount of mana")
    void multipleBottlesResolveIndependently() {
        Permanent first = addBottle(player1);
        Permanent second = addBottle(player1);
        first.setCounterCount(CounterType.CHARGE, 2);
        second.setCounterCount(CounterType.CHARGE, 3);

        advanceToPrecombatMain(player1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(first.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(second.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(5);
    }

    private Permanent addBottle(Player player) {
        return harness.addToBattlefieldAndReturn(player, new VentifactBottle());
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
