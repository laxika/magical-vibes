package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetOpponentOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SunCrownedHuntersTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Sun-Crowned Hunters has one ON_DEALT_DAMAGE effect of type DealDamageToTargetOpponentOrPlaneswalkerEffect(3)")
    void hasCorrectEffect() {
        SunCrownedHunters card = new SunCrownedHunters();

        assertThat(card.getEffects(EffectSlot.ON_DEALT_DAMAGE)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEALT_DAMAGE).getFirst())
                .isInstanceOf(DealDamageToTargetOpponentOrPlaneswalkerEffect.class);
        DealDamageToTargetOpponentOrPlaneswalkerEffect effect =
                (DealDamageToTargetOpponentOrPlaneswalkerEffect) card.getEffects(EffectSlot.ON_DEALT_DAMAGE).getFirst();
        assertThat(effect.damage()).isEqualTo(3);
    }

    // ===== Non-combat damage trigger =====

    @Test
    @DisplayName("When dealt non-lethal spell damage, deals 3 damage to opponent")
    void spellDamageDeals3ToOpponent() {
        harness.addToBattlefield(player2, new SunCrownedHunters());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        UUID huntersId = harness.getPermanentId(player2, "Sun-Crowned Hunters");
        harness.castInstant(player1, 0, huntersId);
        harness.passBothPriorities(); // Resolve Shock — 2 damage to Hunters (non-lethal for 5/4)

        // ON_DEALT_DAMAGE trigger should be on the stack with target set to opponent
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getTargetId()).isEqualTo(player1.getId());

        // Resolve the trigger
        harness.passBothPriorities();

        // Hunters should survive (5/4 takes 2 damage)
        harness.assertOnBattlefield(player2, "Sun-Crowned Hunters");

        // Opponent (player1) should take 3 damage
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Controller does not take damage from their own Hunters' trigger")
    void controllerDoesNotTakeDamage() {
        harness.addToBattlefield(player2, new SunCrownedHunters());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        UUID huntersId = harness.getPermanentId(player2, "Sun-Crowned Hunters");
        harness.castInstant(player1, 0, huntersId);
        harness.passBothPriorities(); // Resolve Shock
        harness.passBothPriorities(); // Resolve trigger

        // Controller (player2) should NOT take damage from their own Hunters
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Trigger goes on stack =====

    @Test
    @DisplayName("Trigger puts a triggered ability on the stack")
    void triggerGoesOnStack() {
        harness.addToBattlefield(player2, new SunCrownedHunters());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID huntersId = harness.getPermanentId(player2, "Sun-Crowned Hunters");
        harness.castInstant(player1, 0, huntersId);
        harness.passBothPriorities(); // Resolve Shock

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Sun-Crowned Hunters");
    }

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("When dealt non-lethal combat damage, enrage trigger fires and deals 3 to opponent")
    void combatDamageTriggersEnrage() {
        harness.addToBattlefield(player2, new SunCrownedHunters());
        harness.addToBattlefield(player1, new FugitiveWizard()); // 1/1 attacker

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent hunters = gd.playerBattlefields.get(player2.getId()).getFirst();
        hunters.setSummoningSick(false);
        hunters.setBlocking(true);
        hunters.addBlockingTarget(0);

        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Resolve combat damage — auto-pass will resolve the trigger too
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Hunters should survive (5/4 takes 1 damage from 1/1)
        harness.assertOnBattlefield(player2, "Sun-Crowned Hunters");

        // Fugitive Wizard should die (1/1 takes 5 damage)
        harness.assertInGraveyard(player1, "Fugitive Wizard");

        // Opponent (player1) should have taken 3 damage from the trigger
        assertThat(gd.playerLifeTotals.get(player1.getId())).isLessThan(20);
    }

    // ===== Combat damage deals exact 3 damage =====

    @Test
    @DisplayName("Combat enrage trigger deals exactly 3 damage to opponent")
    void combatDamageDealsExact3() {
        harness.addToBattlefield(player2, new SunCrownedHunters());
        harness.addToBattlefield(player1, new FugitiveWizard()); // 1/1 attacker

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent hunters = gd.playerBattlefields.get(player2.getId()).getFirst();
        hunters.setSummoningSick(false);
        hunters.setBlocking(true);
        hunters.addBlockingTarget(0);

        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Resolve combat damage and enrage trigger
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Opponent should have taken exactly 3 damage from the enrage trigger
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    // ===== No damage, no trigger =====

    @Test
    @DisplayName("No trigger fires when Hunters are not dealt damage")
    void noTriggerWithoutDamage() {
        harness.addToBattlefield(player2, new SunCrownedHunters());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // No damage to opponent
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Lethal damage still triggers =====

    @Test
    @DisplayName("Enrage triggers even when Hunters take lethal damage")
    void lethalDamageStillTriggers() {
        harness.addToBattlefield(player2, new SunCrownedHunters());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player1, 20);

        UUID huntersId = harness.getPermanentId(player2, "Sun-Crowned Hunters");

        // First Shock — 2 damage
        harness.castInstant(player1, 0, huntersId);
        harness.passBothPriorities(); // Resolve first Shock
        harness.passBothPriorities(); // Resolve first trigger (3 to opponent)

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);

        // Second Shock — 2 more damage (4 total on 5/4 = lethal)
        huntersId = harness.getPermanentId(player2, "Sun-Crowned Hunters");
        harness.castInstant(player1, 0, huntersId);
        harness.passBothPriorities(); // Resolve second Shock — triggers enrage, Hunters die

        // Enrage trigger should be on the stack even though Hunters died
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // Resolve second trigger

        // Hunters should be dead
        harness.assertInGraveyard(player2, "Sun-Crowned Hunters");

        // Opponent should have taken 3 + 3 = 6 damage total from triggers
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }
}
