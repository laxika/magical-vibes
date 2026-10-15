package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TimeBeetle.class, GrizzlyBears.class, TheTenthDoctor.class})
class TimeBeetleTest extends BaseCardTest {

    @Test
    void timeTravelsWhenItDealsCombatDamageToAPlayer() {
        Permanent beetle = addCreatureReady(player1, new TimeBeetle());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.TIME, 1);
        beetle.setAttacking(true);

        resolveCombat();
        harness.handleListChoice(player1, "ADD");

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    void canRemoveTheLastTimeCounterFromAControlledPermanent() {
        Permanent beetle = addCreatureReady(player1, new TimeBeetle());
        Permanent target = addCreatureReady(player1, new TimeBeetle());
        target.setCounterCount(CounterType.TIME, 1);
        beetle.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();
        harness.handleListChoice(player1, "REMOVE");

        assertThat(target.getCounterCount(CounterType.TIME)).isZero();
    }

    @Test
    void canDeclineToChangeATimeCounter() {
        Permanent beetle = addCreatureReady(player1, new TimeBeetle());
        Permanent target = addCreatureReady(player1, new TimeBeetle());
        target.setCounterCount(CounterType.TIME, 2);
        beetle.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();
        harness.handleListChoice(player1, "SKIP");

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void ignoresOpponentsPermanentsAndControlledPermanentsWithoutTimeCounters() {
        Permanent beetle = addCreatureReady(player1, new TimeBeetle());
        Permanent opponent = addCreatureReady(player2, new TimeBeetle());
        opponent.setCounterCount(CounterType.TIME, 2);
        beetle.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(beetle.getCounterCount(CounterType.TIME)).isZero();
        assertThat(opponent.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 19);
    }

    @Test
    void skulkPreventsBlockingByACreatureWithGreaterPower() {
        Permanent beetle = addCreatureReady(player1, new TimeBeetle());
        addCreatureReady(player2, new TheTenthDoctor());
        beetle.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("skulk");
    }

    @Test
    void equalPowerCreatureCanBlockAndCombatDamageToItDoesNotTimeTravel() {
        Permanent beetle = addCreatureReady(player1, new TimeBeetle());
        Permanent target = addCreatureReady(player1, new TimeBeetle());
        target.setCounterCount(CounterType.TIME, 1);
        Permanent blocker = addCreatureReady(player2, new TimeBeetle());
        beetle.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
        resolveCombat();
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    void canRemoveATimeCounterFromAnOwnedSuspendedCard() {
        harness.addToBattlefield(player1, new TheTenthDoctor());
        addCreatureReady(player1, new TimeBeetle());
        TimeBeetle suspended = new TimeBeetle();
        harness.setLibrary(player1, List.of(suspended));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            resolveAllTriggers();
        });
        assertThat(gd.exiledCardTimeCounters).containsEntry(suspended.getId(), 3);

        resolveCombat();
        resolveAllTriggers();
        harness.handleListChoice(player1, "REMOVE");

        assertThat(gd.exiledCardTimeCounters).containsEntry(suspended.getId(), 2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void counterChangesWaitUntilAllTimeTravelChoicesHaveBeenMade() {
        Permanent beetle = addCreatureReady(player1, new TimeBeetle());
        Permanent first = addCreatureReady(player1, new TimeBeetle());
        Permanent second = addCreatureReady(player1, new TimeBeetle());
        first.setCounterCount(CounterType.TIME, 1);
        second.setCounterCount(CounterType.TIME, 1);
        beetle.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();
        harness.handleListChoice(player1, "REMOVE");

        assertThat(first.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.TIME)).isEqualTo(1);

        harness.handleListChoice(player1, "REMOVE");

        assertThat(first.getCounterCount(CounterType.TIME)).isZero();
        assertThat(second.getCounterCount(CounterType.TIME)).isZero();
    }
}
