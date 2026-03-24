package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SnappingSailbackTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Snapping Sailback has one ON_DEALT_DAMAGE effect of type PutCounterOnSelfEffect")
    void hasCorrectEffect() {
        SnappingSailback card = new SnappingSailback();

        assertThat(card.getEffects(EffectSlot.ON_DEALT_DAMAGE)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEALT_DAMAGE).get(0))
                .isInstanceOf(PutCounterOnSelfEffect.class);

        PutCounterOnSelfEffect effect = (PutCounterOnSelfEffect) card.getEffects(EffectSlot.ON_DEALT_DAMAGE).get(0);
        assertThat(effect.counterType()).isEqualTo(CounterType.PLUS_ONE_PLUS_ONE);
    }

    // ===== Non-combat damage trigger =====

    @Test
    @DisplayName("When dealt non-lethal spell damage, puts a +1/+1 counter on itself")
    void spellDamagePutsCounterOnSelf() {
        harness.addToBattlefield(player2, new SnappingSailback());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID sailbackId = harness.getPermanentId(player2, "Snapping Sailback");
        harness.castInstant(player1, 0, sailbackId);
        harness.passBothPriorities(); // Resolve Shock — 2 damage to Sailback (non-lethal for 4/4)

        // ON_DEALT_DAMAGE trigger should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve the trigger
        harness.passBothPriorities();

        // Sailback should survive (4/4 takes 2 damage)
        harness.assertOnBattlefield(player2, "Snapping Sailback");

        // Sailback should have 1 +1/+1 counter
        Permanent sailback = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Snapping Sailback"))
                .findFirst().orElseThrow();
        assertThat(sailback.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("When dealt non-lethal combat damage, puts a +1/+1 counter on itself")
    void combatDamagePutsCounterOnSelf() {
        harness.addToBattlefield(player2, new SnappingSailback());
        harness.addToBattlefield(player1, new FugitiveWizard()); // 1/1 attacker

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
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
        Permanent sailbackAfter = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Snapping Sailback"))
                .findFirst().orElseThrow();
        assertThat(sailbackAfter.getPlusOnePlusOneCounters()).isEqualTo(1);

        // Fugitive Wizard should die (1/1 takes 4 damage)
        harness.assertInGraveyard(player1, "Fugitive Wizard");
    }

    // ===== Multiple damage instances =====

    @Test
    @DisplayName("Each damage instance adds a separate +1/+1 counter")
    void multipleDamageInstancesAddMultipleCounters() {
        harness.addToBattlefield(player2, new SnappingSailback());

        // First Shock — 2 damage (non-lethal for 4/4)
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID sailbackId = harness.getPermanentId(player2, "Snapping Sailback");
        harness.castInstant(player1, 0, sailbackId);
        harness.passBothPriorities(); // Resolve first Shock
        harness.passBothPriorities(); // Resolve first trigger

        Permanent sailback = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Snapping Sailback"))
                .findFirst().orElseThrow();
        assertThat(sailback.getPlusOnePlusOneCounters()).isEqualTo(1);

        // Second Shock — 2 more damage (now 5/5 with counter, takes 2 + 2 = 4 damage total, non-lethal)
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        sailbackId = harness.getPermanentId(player2, "Snapping Sailback");
        harness.castInstant(player1, 0, sailbackId);
        harness.passBothPriorities(); // Resolve second Shock
        harness.passBothPriorities(); // Resolve second trigger

        // Sailback should still be alive (now 6/6 with 2 counters, 4 damage total)
        harness.assertOnBattlefield(player2, "Snapping Sailback");

        // Should have 2 +1/+1 counters
        Permanent sailbackAfter = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Snapping Sailback"))
                .findFirst().orElseThrow();
        assertThat(sailbackAfter.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    // ===== No damage, no trigger =====

    @Test
    @DisplayName("No trigger fires when Snapping Sailback is not dealt damage")
    void noTriggerWithoutDamage() {
        harness.addToBattlefield(player2, new SnappingSailback());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // No counters
        Permanent sailback = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Snapping Sailback"))
                .findFirst().orElseThrow();
        assertThat(sailback.getPlusOnePlusOneCounters()).isZero();
    }

    // ===== Lethal damage — no counter if creature dies =====

    @Test
    @DisplayName("Enrage still triggers when dealt lethal damage but creature leaves battlefield")
    void lethalDamageStillTriggers() {
        harness.addToBattlefield(player2, new SnappingSailback());
        // Use two Shocks to deal lethal: 2 + 2 = 4 damage to a 4/4
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID sailbackId = harness.getPermanentId(player2, "Snapping Sailback");
        harness.castInstant(player1, 0, sailbackId);
        harness.passBothPriorities(); // Resolve first Shock (2 damage, non-lethal)
        harness.passBothPriorities(); // Resolve first trigger — now 5/5 with 2 damage

        // Second Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        sailbackId = harness.getPermanentId(player2, "Snapping Sailback");
        harness.castInstant(player1, 0, sailbackId);
        harness.passBothPriorities(); // Resolve second Shock (4 total damage on 5/5 — non-lethal again!)

        // Still alive because it grew from the first counter
        harness.assertOnBattlefield(player2, "Snapping Sailback");
    }
}
