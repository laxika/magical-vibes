package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

class StromkirkPatrolTest extends BaseCardTest {

    private Permanent addReadyPatrol() {
        Permanent perm = new Permanent(new StromkirkPatrol());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    // ===== Combat damage +1/+1 counter trigger =====

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

        // 3/5 blocker kills the 4/3 Patrol (3 damage >= 3 toughness)
        Permanent blocker = new Permanent(new HillGiant());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Patrol should be dead
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Stromkirk Patrol"));
    }
}
