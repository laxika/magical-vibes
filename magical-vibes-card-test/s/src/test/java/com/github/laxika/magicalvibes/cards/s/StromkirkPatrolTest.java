package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RottingFensnake;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StromkirkPatrol.class, RottingFensnake.class})
class StromkirkPatrolTest extends BaseCardTest {

    private Permanent addReadyPatrol() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new StromkirkPatrol());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when dealing combat damage to a player")
    void getsCounterOnCombatDamage() {
        Permanent patrol = addReadyPatrol();
        patrol.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // through combat damage

        // Player2 takes 4 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Patrol should have a +1/+1 counter
        assertThat(patrol.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals increased combat damage after getting a +1/+1 counter")
    void dealsMoreDamageWithCounter() {
        Permanent patrol = addReadyPatrol();
        patrol.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // simulate having gotten a counter previously
        patrol.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // 4 base power + 1 from counter = 5 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);

        // Resolve trigger — gets another counter
        harness.passBothPriorities();
        assertThat(patrol.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("No counter when blocked and killed")
    void noCounterWhenBlockedAndKilled() {
        Permanent patrol = addReadyPatrol();
        patrol.setAttacking(true);

        // The 5/1 blocker and the 4/3 Patrol deal lethal damage to each other.
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RottingFensnake());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Patrol should be dead
        harness.assertInGraveyard(player1, "Stromkirk Patrol");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Surviving combat with a creature does not add a counter")
    void noCounterWhenBlockedAndSurviving() {
        Permanent patrol = addReadyPatrol();
        patrol.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        patrol.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new StromkirkPatrol());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stromkirk Patrol");
        harness.assertLife(player2, 20);
        assertThat(patrol.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Patrol gets its own counter only when its trigger resolves")
    void eachPatrolGetsItsOwnCounter() {
        Permanent first = addReadyPatrol();
        Permanent second = addReadyPatrol();
        first.setAttacking(true);
        second.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertLife(player2, 12);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
