package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SnappingSailback.class, Shock.class, FugitiveWizard.class})
class SnappingSailbackTest extends BaseCardTest {

    @Test
    @DisplayName("When dealt non-lethal spell damage, puts a +1/+1 counter on itself")
    void spellDamagePutsCounterOnSelf() {
        harness.addToBattlefield(player2, new SnappingSailback());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID sailbackId = harness.getPermanentId(player2, "Snapping Sailback");
        harness.castAndResolveInstant(player1, 0, sailbackId);

        // ON_DEALT_DAMAGE trigger should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve the trigger
        harness.passBothPriorities();

        // Sailback should survive (4/4 takes 2 damage)
        harness.assertOnBattlefield(player2, "Snapping Sailback");

        // Sailback should have 1 +1/+1 counter
        Permanent sailback = findPermanent(player2, "Snapping Sailback");
        assertThat(sailback.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("When dealt non-lethal combat damage, puts a +1/+1 counter on itself")
    void combatDamagePutsCounterOnSelf() {
        harness.addToBattlefield(player2, new SnappingSailback());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent sailback = gd.playerBattlefields.get(player2.getId()).getFirst();
        sailback.setSummoningSick(false);
        sailback.setBlocking(true);
        sailback.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Resolve combat damage and trigger
        harness.passBothPriorities(); // combat damage
        harness.passBothPriorities(); // trigger on stack
        harness.passBothPriorities(); // resolve trigger

        // Sailback should survive (4/4 takes 1 damage from 1/1)
        harness.assertOnBattlefield(player2, "Snapping Sailback");

        // Sailback should have a +1/+1 counter
        Permanent sailbackAfter = findPermanent(player2, "Snapping Sailback");
        assertThat(sailbackAfter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        // Fugitive Wizard should die (1/1 takes 4 damage)
        harness.assertInGraveyard(player1, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Each damage instance adds a separate +1/+1 counter")
    void multipleDamageInstancesAddMultipleCounters() {
        harness.addToBattlefield(player2, new SnappingSailback());

        // First Shock — 2 damage (non-lethal for 4/4)
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID sailbackId = harness.getPermanentId(player2, "Snapping Sailback");
        harness.castAndResolveInstant(player1, 0, sailbackId);
        harness.passBothPriorities(); // Resolve first trigger

        Permanent sailback = findPermanent(player2, "Snapping Sailback");
        assertThat(sailback.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        // Second Shock — 2 more damage (now 5/5 with counter, takes 2 + 2 = 4 damage total, non-lethal)
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        sailbackId = harness.getPermanentId(player2, "Snapping Sailback");
        harness.castAndResolveInstant(player1, 0, sailbackId);
        harness.passBothPriorities(); // Resolve second trigger

        // Sailback should still be alive (now 6/6 with 2 counters, 4 damage total)
        harness.assertOnBattlefield(player2, "Snapping Sailback");

        // Should have 2 +1/+1 counters
        Permanent sailbackAfter = findPermanent(player2, "Snapping Sailback");
        assertThat(sailbackAfter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("No trigger fires when Snapping Sailback is not dealt damage")
    void noTriggerWithoutDamage() {
        harness.addToBattlefield(player2, new SnappingSailback());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // No counters
        Permanent sailback = findPermanent(player2, "Snapping Sailback");
        assertThat(sailback.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Lethal damage triggers enrage but cannot put counters on the dead creature")
    void lethalDamageStillTriggers() {
        Permanent sailback = harness.addToBattlefieldAndReturn(player2, new SnappingSailback());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, sailback.getId());
        assertThat(gd.stack).hasSize(1);

        // Deal the second two damage before the first enrage ability resolves.
        harness.castAndResolveInstant(player1, 0, sailback.getId());
        harness.assertNotOnBattlefield(player2, "Snapping Sailback");
        harness.assertInGraveyard(player2, "Snapping Sailback");
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(sailback.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Flash allows casting during an opponent's combat")
    void canBeCastDuringOpponentsCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player2, List.of(new SnappingSailback()));
        harness.addMana(player2, ManaColor.GREEN, 5);

        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Snapping Sailback");
        harness.assertNotInHand(player2, "Snapping Sailback");
    }
}
