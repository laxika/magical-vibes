package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(OsaiVultures.class)
class OsaiVulturesTest extends BaseCardTest {

    private void advanceToEndStepAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Gains a carrion counter at end step when a creature died this turn")
    void gainsCounterWhenCreatureDied() {
        Permanent vultures = addReadyVultures(player1);

        gd.creatureDeathCountThisTurn.merge(player2.getId(), 3, Integer::sum);

        advanceToEndStepAndResolve(player1);

        assertThat(vultures.getCounterCount(CounterType.CARRION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gains no counter at end step when no creature died this turn")
    void noCounterWhenNoDeath() {
        Permanent vultures = addReadyVultures(player1);

        advanceToEndStepAndResolve(player1);

        assertThat(vultures.getCounterCount(CounterType.CARRION)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void gainsCounterDuringOpponentsEndStep() {
        Permanent vultures = addReadyVultures(player1);

        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        advanceToEndStepAndResolve(player2);

        assertThat(vultures.getCounterCount(CounterType.CARRION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing two carrion counters gives +1/+1 until end of turn")
    void removeTwoCountersForBoost() {
        Permanent vultures = addReadyVultures(player1);
        vultures.setCounterCount(CounterType.CARRION, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(vultures.getCounterCount(CounterType.CARRION)).isOne();
        assertThat(vultures.getEffectivePower()).isEqualTo(2);
        assertThat(vultures.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate the ability with fewer than two carrion counters")
    void cannotActivateWithoutEnoughCounters() {
        Permanent vultures = addReadyVultures(player1);
        vultures.setCounterCount(CounterType.CARRION, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    void canActivateWhileSummoningSick() {
        Permanent vultures = harness.addToBattlefieldAndReturn(player1, new OsaiVultures());
        vultures.setCounterCount(CounterType.CARRION, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(vultures.getCounterCount(CounterType.CARRION)).isZero();
        assertThat(vultures.getEffectivePower()).isEqualTo(2);
        assertThat(vultures.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void boostExpiresAtEndOfTurn() {
        Permanent vultures = addReadyVultures(player1);
        vultures.setCounterCount(CounterType.CARRION, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(vultures.getEffectivePower()).isEqualTo(2);
        assertThat(vultures.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(vultures.getEffectivePower()).isEqualTo(1);
        assertThat(vultures.getEffectiveToughness()).isEqualTo(1);
    }

    private Permanent addReadyVultures(Player player) {
        return addCreatureReady(player, new OsaiVultures());
    }
}
