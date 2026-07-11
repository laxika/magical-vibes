package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

class FalkenrathMaraudersTest extends BaseCardTest {

    private Permanent addReadyMarauders() {
        Permanent perm = new Permanent(new FalkenrathMarauders());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    // ===== Combat damage +1/+1 counter trigger =====

    @Test
    @DisplayName("Gets two +1/+1 counters when dealing combat damage to a player")
    void getsTwoCountersOnCombatDamage() {
        Permanent marauders = addReadyMarauders();
        marauders.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // through combat damage

        // Player2 takes 2 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Marauders should have two +1/+1 counters
        assertThat(marauders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals increased combat damage after getting counters")
    void dealsMoreDamageWithCounters() {
        Permanent marauders = addReadyMarauders();
        marauders.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2); // simulate having gotten counters previously
        marauders.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // 2 base power + 2 from counters = 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        // Resolve trigger — gets two more counters
        harness.passBothPriorities();
        assertThat(marauders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("No counters when blocked and killed")
    void noCountersWhenBlockedAndKilled() {
        Permanent marauders = addReadyMarauders();
        marauders.setAttacking(true);

        // 4/4 blocker kills the 2/2 Marauders
        Permanent blocker = new Permanent(new SerraAngel());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Marauders should be dead
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Falkenrath Marauders"));
    }
}
