package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGetsPoisonCounterEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReaperOfSheoldredTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Reaper of Sheoldred has one ON_DEALT_DAMAGE poison counter effect")
    void hasCorrectEffect() {
        ReaperOfSheoldred card = new ReaperOfSheoldred();

        assertThat(card.getEffects(EffectSlot.ON_DEALT_DAMAGE)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEALT_DAMAGE).getFirst())
                .isInstanceOf(DamageSourceControllerGetsPoisonCounterEffect.class);
    }

    // ===== Non-combat damage trigger =====

    @Test
    @DisplayName("Shock dealing damage to Reaper gives source controller a poison counter")
    void spellDamageGivesPoisonCounter() {
        harness.addToBattlefield(player2, new ReaperOfSheoldred());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID reaperId = harness.getPermanentId(player2, "Reaper of Sheoldred");
        harness.castInstant(player1, 0, reaperId);
        harness.passBothPriorities(); // Resolve Shock — 2 damage to Reaper

        GameData gd = harness.getGameData();

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
        harness.addToBattlefield(player2, new ReaperOfSheoldred());
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent reaper = gd.playerBattlefields.get(player2.getId()).getFirst();
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
        harness.addToBattlefield(player1, new ReaperOfSheoldred());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent reaper = gd.playerBattlefields.get(player1.getId()).getFirst();
        reaper.setSummoningSick(false);
        reaper.setAttacking(true);

        Permanent blocker1 = gd.playerBattlefields.get(player2.getId()).get(0);
        blocker1.setSummoningSick(false);
        blocker1.setBlocking(true);
        blocker1.addBlockingTarget(0);

        Permanent blocker2 = gd.playerBattlefields.get(player2.getId()).get(1);
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

        // Resolve combat damage and all triggers
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Player2 should have 2 poison counters (one per blocker source)
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }
}
