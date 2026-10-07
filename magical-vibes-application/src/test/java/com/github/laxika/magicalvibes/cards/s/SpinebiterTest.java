package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TriumphOfTheHordes;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Spinebiter.class, GrizzlyBears.class, TriumphOfTheHordes.class})
class SpinebiterTest extends BaseCardTest {

    

    @Test
    @DisplayName("Unblocked Spinebiter deals poison counters instead of life loss")
    void dealsPoisonCountersWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent spinebiter = harness.addToBattlefieldAndReturn(player1, new Spinebiter());
        spinebiter.setSummoningSick(false);
        spinebiter.setAttacking(true);

        resolveCombat();

        // Life should remain unchanged (infect deals poison, not life loss)
        harness.assertLife(player2, 20);
        // Poison counters should equal power (3)
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
    }

    @Test
    @DisplayName("Blocked Spinebiter can assign combat damage to defending player as poison counters")
    void blockedSpinebiterAssignsDamageToDefendingPlayerAsPoison() {
        harness.setLife(player2, 20);
        Permanent spinebiter = harness.addToBattlefieldAndReturn(player1, new Spinebiter());
        spinebiter.setSummoningSick(false);
        spinebiter.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Assign all damage to defending player (assign as though unblocked)
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 3));

        // Life should remain unchanged (infect deals poison, not life loss)
        harness.assertLife(player2, 20);
        // Poison counters should equal power (3)
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
        // Blocker should survive (no damage assigned to it)
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Blocked Spinebiter can assign damage to blocker as -1/-1 counters")
    void blockedSpinebiterAssignsDamageToBlockerAsMinusCounters() {
        harness.setLife(player2, 20);
        Permanent spinebiter = harness.addToBattlefieldAndReturn(player1, new Spinebiter());
        spinebiter.setSummoningSick(false);
        spinebiter.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Assign all damage to blocker instead of defending player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3));

        // Grizzly Bears (2/2) takes 3 infect damage → 3 -1/-1 counters → dies
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        // No poison counters — damage went to a creature
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
        // Life unchanged
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Assigning as though unblocked still allows the blocker to deal infect damage")
    void blockerStillDealsDamageWhenBypassed() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new Spinebiter());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new Spinebiter());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 3));

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Spinebiter");
        harness.assertOnBattlefield(player2, "Spinebiter");
    }

    @Test
    @DisplayName("Spinebiter cannot split damage between a blocker and the defending player without trample")
    void cannotPartiallyBypassBlocker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new Spinebiter());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new Spinebiter());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 3));
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gaining trample does not prevent Spinebiter from bypassing blockers entirely")
    void canAssignAllDamageAsThoughUnblockedWithTrample() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new Spinebiter());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new Spinebiter());
        harness.castFromHand(player1, new TriumphOfTheHordes(), "{2}{G}{G}");
        harness.passBothPriorities();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 4));

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(4);
        harness.assertLife(player2, 20);
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Spinebiter");
    }
}
