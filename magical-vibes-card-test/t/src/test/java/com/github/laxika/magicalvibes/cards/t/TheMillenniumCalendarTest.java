package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MirrorGallery;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.turn.UntapStepService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheMillenniumCalendar.class, MirrorGallery.class})
class TheMillenniumCalendarTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one time counter on each Calendar for each permanent untapped during the untap step")
    void countsPermanentsUntappedDuringUntapStep() {
        harness.addToBattlefield(player1, new MirrorGallery());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TheMillenniumCalendar());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TheMillenniumCalendar());
        first.tap();
        second.tap();

        runUntapStep(player1);

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    @DisplayName("Doubles its time counters when its activated ability resolves")
    void doublesTimeCounters() {
        Permanent calendar = harness.addToBattlefieldAndReturn(player1, new TheMillenniumCalendar());
        calendar.setCounterCount(CounterType.TIME, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(calendar.getCounterCount(CounterType.TIME)).isEqualTo(6);
        assertThat(calendar.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrifices itself and makes each opponent lose 1000 life at 1000 time counters")
    void sacrificesAndDrainsOpponentsAtThreshold() {
        Permanent calendar = harness.addToBattlefieldAndReturn(player1, new TheMillenniumCalendar());
        calendar.setCounterCount(CounterType.TIME, 1000);
        harness.setLife(player2, 2000);

        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(calendar.getCard());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(1000);
    }

    private void runUntapStep(Player untappingPlayer) {
        harness.performUntapStep(untappingPlayer);
        harness.inMutationScope(() -> GameTestEngineContext.get()
                .getBean(UntapStepService.class)
                .finishUntapStep(gd, untappingPlayer.getId()));
    }

    @Test
    void doesNotTriggerWhenNoPermanentBecomesUntapped() {
        Permanent calendar = harness.addToBattlefieldAndReturn(player1, new TheMillenniumCalendar());

        runUntapStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(calendar.getCounterCount(CounterType.TIME)).isZero();
    }

    @Test
    void doesNotCountOpponentsUntapStep() {
        Permanent calendar = harness.addToBattlefieldAndReturn(player1, new TheMillenniumCalendar());
        Permanent opposingCalendar = harness.addToBattlefieldAndReturn(player2, new TheMillenniumCalendar());
        calendar.tap();
        opposingCalendar.tap();

        runUntapStep(player2);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(calendar.getCounterCount(CounterType.TIME)).isZero();
        assertThat(calendar.isTapped()).isTrue();
        assertThat(opposingCalendar.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    void doublingUsesCounterCountAtResolutionAndLeavesOtherCountersAlone() {
        Permanent calendar = harness.addToBattlefieldAndReturn(player1, new TheMillenniumCalendar());
        calendar.setCounterCount(CounterType.TIME, 3);
        calendar.setCounterCount(CounterType.CHARGE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        calendar.setCounterCount(CounterType.TIME, 5);
        harness.passBothPriorities();

        assertThat(calendar.getCounterCount(CounterType.TIME)).isEqualTo(10);
        assertThat(calendar.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    void doublingZeroCountersDoesNotAddAny() {
        Permanent calendar = harness.addToBattlefieldAndReturn(player1, new TheMillenniumCalendar());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(calendar.getCounterCount(CounterType.TIME)).isZero();
        assertThat(calendar.isTapped()).isTrue();
    }

    @Test
    void doesNotTriggerBelowThresholdOrDuplicatePendingStateTrigger() {
        Permanent calendar = harness.addToBattlefieldAndReturn(player1, new TheMillenniumCalendar());
        calendar.setCounterCount(CounterType.TIME, 999);
        harness.runStateBasedActions();
        assertThat(gd.stack).isEmpty();

        calendar.setCounterCount(CounterType.TIME, 1001);
        harness.runStateBasedActions();
        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void stateTriggerStillResolvesAfterCountersFallBelowThreshold() {
        Permanent calendar = harness.addToBattlefieldAndReturn(player1, new TheMillenniumCalendar());
        calendar.setCounterCount(CounterType.TIME, 1000);
        harness.setLife(player2, 2000);
        harness.runStateBasedActions();
        calendar.setCounterCount(CounterType.TIME, 0);

        resolveAllTriggers();

        harness.assertInGraveyard(player1, "The Millennium Calendar");
        harness.assertNotOnBattlefield(player1, "The Millennium Calendar");
        harness.assertLife(player2, 1000);
        harness.assertLife(player1, 20);
    }

    @Test
    void doublingToThresholdQueuesSacrificeAndLifeLoss() {
        Permanent calendar = harness.addToBattlefieldAndReturn(player1, new TheMillenniumCalendar());
        calendar.setCounterCount(CounterType.TIME, 500);
        harness.setLife(player2, 2000);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(calendar.getCounterCount(CounterType.TIME)).isEqualTo(1000);
        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "The Millennium Calendar");
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "The Millennium Calendar");
        harness.assertNotOnBattlefield(player1, "The Millennium Calendar");
        harness.assertLife(player2, 1000);
        harness.assertLife(player1, 20);
    }
}
