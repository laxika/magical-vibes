package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

class ErdwalRipperTest extends BaseCardTest {

    private Permanent addReadyRipper() {
        Permanent perm = new Permanent(new ErdwalRipper());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    // ===== Combat damage +1/+1 counter trigger =====

    @Test
    @DisplayName("Gets a +1/+1 counter when dealing combat damage to a player")
    void getsCounterOnCombatDamage() {
        Permanent ripper = addReadyRipper();
        ripper.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // through combat damage

        // Player2 takes 2 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Ripper should have a +1/+1 counter
        assertThat(ripper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals increased combat damage after getting a +1/+1 counter")
    void dealsMoreDamageWithCounter() {
        Permanent ripper = addReadyRipper();
        ripper.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // simulate having gotten a counter previously
        ripper.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // 2 base power + 1 from counter = 3 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);

        // Resolve trigger — gets another counter
        harness.passBothPriorities();
        assertThat(ripper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("No counter when blocked and dealing no damage to a player")
    void noCounterWhenBlocked() {
        Permanent ripper = addReadyRipper();
        ripper.setAttacking(true);
        harness.setLife(player2, 20);

        // 4/4 blocker blocks the 2/1 Ripper
        Permanent blocker = new Permanent(new SerraAngel());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // No combat damage reaches the player
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        // No counter placed (and Ripper died to the 4/4)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Erdwal Ripper"));
    }
}
