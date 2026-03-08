package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SpinebiterTest extends BaseCardTest {

    @Test
    @DisplayName("Spinebiter has AssignCombatDamageAsThoughUnblockedEffect as static effect")
    void hasCorrectStaticEffect() {
        Spinebiter card = new Spinebiter();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(AssignCombatDamageAsThoughUnblockedEffect.class);
    }

    @Test
    @DisplayName("Unblocked Spinebiter deals poison counters instead of life loss")
    void dealsPoisonCountersWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent spinebiter = new Permanent(new Spinebiter());
        spinebiter.setSummoningSick(false);
        spinebiter.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(spinebiter);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Life should remain unchanged (infect deals poison, not life loss)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Poison counters should equal power (3)
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
    }

    @Test
    @DisplayName("Blocked Spinebiter can assign combat damage to defending player as poison counters")
    void blockedSpinebiterAssignsDamageToDefendingPlayerAsPoison() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Spinebiter());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent spinebiter = gd.playerBattlefields.get(player1.getId()).getFirst();
        spinebiter.setSummoningSick(false);
        spinebiter.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Advance from DECLARE_BLOCKERS → COMBAT_DAMAGE (paused for assignment)
        harness.passBothPriorities();

        // Assign all damage to defending player (assign as though unblocked)
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 3));

        // Life should remain unchanged (infect deals poison, not life loss)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Poison counters should equal power (3)
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
        // Blocker should survive (no damage assigned to it)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Blocked Spinebiter can assign damage to blocker as -1/-1 counters")
    void blockedSpinebiterAssignsDamageToBlockerAsMinusCounters() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Spinebiter());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent spinebiter = gd.playerBattlefields.get(player1.getId()).getFirst();
        spinebiter.setSummoningSick(false);
        spinebiter.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Advance from DECLARE_BLOCKERS → COMBAT_DAMAGE (paused for assignment)
        harness.passBothPriorities();

        // Assign all damage to blocker instead of defending player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3));

        // Grizzly Bears (2/2) takes 3 infect damage → 3 -1/-1 counters → dies
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        // No poison counters — damage went to a creature
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
        // Life unchanged
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
