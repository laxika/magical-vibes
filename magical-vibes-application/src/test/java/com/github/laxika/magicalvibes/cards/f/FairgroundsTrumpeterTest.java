package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.w.WakerOfTheWilds;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FairgroundsTrumpeter.class, WakerOfTheWilds.class, Forest.class})
class FairgroundsTrumpeterTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself at each end step after a counter was put on a permanent you control")
    void growsAfterCounterWasPutOnControlledPermanent() {
        Permanent trumpeter = harness.addToBattlefieldAndReturn(player1, new FairgroundsTrumpeter());
        harness.addToBattlefield(player1, new WakerOfTheWilds());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 1, 0, 3, land.getId());
        harness.passBothPriorities();

        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(trumpeter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger when an opponent puts a counter on their own permanent")
    void doesNotGrowFromOpponentCounterPlacement() {
        Permanent trumpeter = harness.addToBattlefieldAndReturn(player1, new FairgroundsTrumpeter());
        harness.addToBattlefield(player2, new WakerOfTheWilds());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, 0, 0, 2, land.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(trumpeter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Triggers during the opponent's end step after your permanent receives a counter")
    void growsDuringOpponentsEndStep() {
        Permanent trumpeter = harness.addToBattlefieldAndReturn(player1, new FairgroundsTrumpeter());
        harness.addToBattlefield(player1, new WakerOfTheWilds());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 1, 0, 1, land.getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(trumpeter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts counter placement earlier in the turn before Trumpeter entered")
    void countsPlacementBeforeEnteringBattlefield() {
        harness.addToBattlefield(player1, new WakerOfTheWilds());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, 0, 1, land.getId());
        harness.passBothPriorities();
        Permanent trumpeter = harness.enterBattlefieldAndReturn(player1, new FairgroundsTrumpeter());
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(trumpeter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger if the first counter is placed after the end step begins")
    void placementDuringEndStepDoesNotTriggerRetroactively() {
        Permanent trumpeter = harness.addToBattlefieldAndReturn(player1, new FairgroundsTrumpeter());
        harness.addToBattlefield(player1, new WakerOfTheWilds());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 1, 0, 1, land.getId());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(trumpeter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
