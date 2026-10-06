package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OgreMenial;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThunderingTanadon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReaperOfSheoldred.class, GrizzlyBears.class, Shock.class, OgreMenial.class, ThunderingTanadon.class})
class ReaperOfSheoldredTest extends BaseCardTest {

    // ===== Non-combat damage trigger =====

    @Test
    @DisplayName("Shock dealing damage to Reaper gives source controller a poison counter")
    void spellDamageGivesPoisonCounter() {
        harness.addToBattlefield(player2, new ReaperOfSheoldred());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID reaperId = harness.getPermanentId(player2, "Reaper of Sheoldred");
        harness.castAndResolveInstant(player1, 0, reaperId);

        // ON_DEALT_DAMAGE trigger should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve the trigger
        harness.passBothPriorities();

        // Player1 (source controller) should have 1 poison counter (one per source, not per damage)
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);

        // Reaper should survive (2/5 takes only 2 damage)
        harness.assertOnBattlefield(player2, "Reaper of Sheoldred");
    }

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("Creature dealing combat damage to Reaper gives attacker's controller a poison counter")
    void combatDamageGivesPoisonCounter() {
        Permanent reaper = harness.addToBattlefieldAndReturn(player2, new ReaperOfSheoldred());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        reaper.setSummoningSick(false);
        reaper.setBlocking(true);
        reaper.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Resolve combat damage and trigger
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Player1 should have 1 poison counter
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);

        // Reaper should survive (2/5 takes 2 damage)
        harness.assertOnBattlefield(player2, "Reaper of Sheoldred");

        // Grizzly Bears should die (2/2 takes 2 damage from Reaper with infect → -1/-1 counters)
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Two blockers dealing damage to Reaper give two poison counters to same controller")
    void twoBlockersGiveTwoPoisonCounters() {
        Permanent reaper = harness.addToBattlefieldAndReturn(player1, new ReaperOfSheoldred());
        Permanent blocker1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent blocker2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        reaper.setSummoningSick(false);
        reaper.setAttacking(true);

        blocker1.setSummoningSick(false);
        blocker1.setBlocking(true);
        blocker1.addBlockingTarget(0);

        blocker2.setSummoningSick(false);
        blocker2.setBlocking(true);
        blocker2.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Advance to COMBAT_DAMAGE — paused for manual damage assignment
        harness.passBothPriorities();

        // Assign Reaper's 2 damage: 1 to each Grizzly Bears
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker1.getId(), 1,
                blocker2.getId(), 1
        ));

        // Combat damage and triggers are auto-resolved by handleCombatDamageAssigned

        // Player2 should have 2 poison counters (one per blocker source)
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage from your own spell gives you the poison counter")
    void ownSpellDamageGivesControllerPoison() {
        harness.addToBattlefield(player1, new ReaperOfSheoldred());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Reaper of Sheoldred"));

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Lethal combat damage still triggers one poison counter")
    void lethalCombatDamageStillTriggers() {
        Permanent reaper = harness.addToBattlefieldAndReturn(player1, new ReaperOfSheoldred());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ThunderingTanadon());
        reaper.setSummoningSick(false);
        reaper.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertInGraveyard(player1, "Reaper of Sheoldred");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Zero-power blocker does not trigger a poison counter")
    void zeroCombatDamageDoesNotTrigger() {
        Permanent reaper = harness.addToBattlefieldAndReturn(player1, new ReaperOfSheoldred());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new OgreMenial());
        reaper.setSummoningSick(false);
        reaper.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        harness.assertOnBattlefield(player1, "Reaper of Sheoldred");
    }

    @Test
    @DisplayName("Infect damage to Reaper still triggers its ability for each controller")
    void infectDamageStillTriggers() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new ReaperOfSheoldred());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ReaperOfSheoldred());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Reaper of Sheoldred");
        harness.assertOnBattlefield(player2, "Reaper of Sheoldred");
    }

    @Test
    @DisplayName("Unblocked Reaper gives poison through infect without triggering its damage-received ability")
    void unblockedCombatDamageGivesInfectPoison() {
        Permanent reaper = harness.addToBattlefieldAndReturn(player1, new ReaperOfSheoldred());
        reaper.setSummoningSick(false);
        reaper.setAttacking(true);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
