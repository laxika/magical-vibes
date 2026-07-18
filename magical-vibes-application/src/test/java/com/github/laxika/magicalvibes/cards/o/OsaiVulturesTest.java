package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OsaiVulturesTest extends BaseCardTest {

    private void advanceToEndStepAndResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // advance to end step (queues any trigger)
        harness.passBothPriorities(); // resolve the trigger
    }

    @Test
    @DisplayName("Gains a carrion counter at end step when a creature died this turn")
    void gainsCounterWhenCreatureDied() {
        Permanent vultures = new Permanent(new OsaiVultures());
        gd.playerBattlefields.get(player1.getId()).add(vultures);

        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        advanceToEndStepAndResolve();

        assertThat(vultures.getCounterCount(CounterType.CARRION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gains no counter at end step when no creature died this turn")
    void noCounterWhenNoDeath() {
        Permanent vultures = new Permanent(new OsaiVultures());
        gd.playerBattlefields.get(player1.getId()).add(vultures);

        advanceToEndStepAndResolve();

        assertThat(vultures.getCounterCount(CounterType.CARRION)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing two carrion counters gives +1/+1 until end of turn")
    void removeTwoCountersForBoost() {
        Permanent vultures = addReadyVultures(player1);
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

    private Permanent addReadyVultures(Player player) {
        Permanent perm = new Permanent(new OsaiVultures());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
