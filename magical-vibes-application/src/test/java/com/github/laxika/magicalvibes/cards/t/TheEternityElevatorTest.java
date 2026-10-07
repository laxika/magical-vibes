package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.IntrepidTenderfoot;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheEternityElevator.class, IntrepidTenderfoot.class})
class TheEternityElevatorTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping it adds three colorless mana")
    void tapsForThreeColorlessMana() {
        Permanent elevator = harness.addToBattlefieldAndReturn(player1, new TheEternityElevator());

        harness.activateAbility(player1, battlefieldIndex(elevator), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(elevator.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Station taps another creature and adds charge counters equal to its power")
    void stationAddsCountersEqualToCreaturePower() {
        Permanent elevator = harness.addToBattlefieldAndReturn(player1, new TheEternityElevator());
        Permanent creature = addCreatureReady(player1, new IntrepidTenderfoot());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, battlefieldIndex(elevator), 1, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(elevator.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Twenty charge counters enable the variable any-color mana ability")
    void twentyChargeCountersEnableVariableManaAbility() {
        Permanent elevator = harness.addToBattlefieldAndReturn(player1, new TheEternityElevator());
        elevator.setCounterCount(CounterType.CHARGE, 20);

        harness.activateAbility(player1, battlefieldIndex(elevator), 2, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(20);
        assertThat(elevator.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The variable mana ability cannot be activated below twenty charge counters")
    void variableManaAbilityRequiresTwentyChargeCounters() {
        Permanent elevator = harness.addToBattlefieldAndReturn(player1, new TheEternityElevator());
        elevator.setCounterCount(CounterType.CHARGE, 19);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(elevator), 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(elevator.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void stationCanUseASummoningSickCreatureWhileElevatorIsTapped() {
        Permanent elevator = harness.addToBattlefieldAndReturn(player1, new TheEternityElevator());
        elevator.tap();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        creature.setSummoningSick(true);
        elevator.setCounterCount(CounterType.CHARGE, 19);

        harness.activateAbility(player1, battlefieldIndex(elevator), 1, null, null);

        assertThat(creature.isTapped()).isTrue();
        assertThat(elevator.getCounterCount(CounterType.CHARGE)).isEqualTo(19);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(elevator.getCounterCount(CounterType.CHARGE)).isEqualTo(21);
        assertThat(elevator.isTapped()).isTrue();
    }

    @Test
    void stationCannotUseATappedCreature() {
        Permanent elevator = harness.addToBattlefieldAndReturn(player1, new TheEternityElevator());
        Permanent creature = addCreatureReady(player1, new IntrepidTenderfoot());
        creature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(elevator), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(elevator.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void stationCannotUseAnOpponentsCreature() {
        Permanent elevator = harness.addToBattlefieldAndReturn(player1, new TheEternityElevator());
        Permanent creature = addCreatureReady(player2, new IntrepidTenderfoot());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(elevator), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(elevator.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void stationCannotBeActivatedOutsideAMainPhase() {
        Permanent elevator = harness.addToBattlefieldAndReturn(player1, new TheEternityElevator());
        Permanent creature = addCreatureReady(player1, new IntrepidTenderfoot());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(elevator), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(elevator.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void stationCannotBeActivatedDuringTheOpponentsTurn() {
        Permanent elevator = harness.addToBattlefieldAndReturn(player1, new TheEternityElevator());
        Permanent creature = addCreatureReady(player1, new IntrepidTenderfoot());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(elevator), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(elevator.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void stationCannotBeActivatedWithAnAbilityOnTheStack() {
        Permanent elevator = harness.addToBattlefieldAndReturn(player1, new TheEternityElevator());
        Permanent creature = addCreatureReady(player1, new IntrepidTenderfoot());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, battlefieldIndex(creature), 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(elevator), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(elevator.getCounterCount(CounterType.CHARGE)).isZero();
        harness.passBothPriorities();
    }

    @Test
    void variableManaUsesAllChargeCountersButIgnoresOtherCounters() {
        Permanent elevator = harness.addToBattlefieldAndReturn(player1, new TheEternityElevator());
        elevator.setCounterCount(CounterType.CHARGE, 27);
        elevator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        harness.activateAbility(player1, battlefieldIndex(elevator), 2, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(27);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(27);
        assertThat(elevator.getCounterCount(CounterType.CHARGE)).isEqualTo(27);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void originalManaAbilityRemainsAvailableAboveTheThreshold() {
        Permanent elevator = harness.addToBattlefieldAndReturn(player1, new TheEternityElevator());
        elevator.setCounterCount(CounterType.CHARGE, 20);

        harness.activateAbility(player1, battlefieldIndex(elevator), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(elevator.getCounterCount(CounterType.CHARGE)).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
