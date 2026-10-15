package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CreakwoodSafewright.class, Island.class})
class CreakwoodSafewrightTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with three -1/-1 counters")
    void entersWithThreeMinusOneMinusOneCounters() {
        harness.setHand(player1, List.of(new CreakwoodSafewright()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent safewright = findPermanent(player1, "Creakwood Safewright");
        assertThat(safewright.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removes a -1/-1 counter at your end step when an Elf is in your graveyard")
    void removesCounterWithElfInGraveyard() {
        Permanent safewright = addSafewrightWithCounters(3);
        harness.setGraveyard(player1, List.of(new CreakwoodSafewright()));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(safewright.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not remove a counter when no Elf is in your graveyard")
    void doesNotRemoveCounterWithoutElfInGraveyard() {
        Permanent safewright = addSafewrightWithCounters(3);
        harness.setGraveyard(player1, List.<Card>of(new Island()));

        advanceToEndStep(player1);

        assertThat(safewright.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger when it has no -1/-1 counters")
    void doesNotTriggerWithoutMinusOneMinusOneCounter() {
        Permanent safewright = addSafewrightWithCounters(0);
        harness.setGraveyard(player1, List.of(new CreakwoodSafewright()));

        advanceToEndStep(player1);

        assertThat(safewright.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An Elf in the opponent's graveyard does not satisfy the condition")
    void opponentGraveyardDoesNotQualify() {
        Permanent safewright = addSafewrightWithCounters(3);
        harness.setGraveyard(player2, List.of(new CreakwoodSafewright()));

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(safewright.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger during the opponent's end step")
    void opponentEndStepDoesNotQualify() {
        Permanent safewright = addSafewrightWithCounters(3);
        harness.setGraveyard(player1, List.of(new CreakwoodSafewright()));

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(safewright.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Rechecks the Elf graveyard condition when the ability resolves")
    void elfLeavingGraveyardStopsCounterRemoval() {
        Permanent safewright = addSafewrightWithCounters(3);
        harness.setGraveyard(player1, List.of(new CreakwoodSafewright()));
        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(safewright.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Adding an Elf after the end step begins does not create a trigger")
    void elfArrivingTooLateDoesNotTrigger() {
        Permanent safewright = addSafewrightWithCounters(3);
        advanceToEndStep(player1);
        assertThat(gd.stack).isEmpty();

        harness.setGraveyard(player1, List.of(new CreakwoodSafewright()));

        assertThat(gd.stack).isEmpty();
        assertThat(safewright.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removes only one counter even with multiple Elves in the graveyard")
    void multipleElvesStillRemoveOnlyOneCounter() {
        Permanent safewright = addSafewrightWithCounters(3);
        harness.setGraveyard(player1, List.of(new CreakwoodSafewright(), new CreakwoodSafewright()));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(safewright.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can remove the last -1/-1 counter")
    void removesLastCounter() {
        Permanent safewright = addSafewrightWithCounters(1);
        harness.setGraveyard(player1, List.of(new CreakwoodSafewright()));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(safewright.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger if the source has no counters even when an Elf is present")
    void zeroCountersPreventTrigger() {
        addSafewrightWithCounters(0);
        harness.setGraveyard(player1, List.of(new CreakwoodSafewright()));

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing the last counter before resolution does not affect other counters")
    void rechecksSourceCountersAtResolution() {
        Permanent safewright = addSafewrightWithCounters(1);
        safewright.setCounterCount(CounterType.CHARGE, 2);
        harness.setGraveyard(player1, List.of(new CreakwoodSafewright()));
        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);

        safewright.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(safewright.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(safewright.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    private Permanent addSafewrightWithCounters(int count) {
        Permanent safewright = harness.addToBattlefieldAndReturn(player1, new CreakwoodSafewright());
        safewright.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, count);
        return safewright;
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
