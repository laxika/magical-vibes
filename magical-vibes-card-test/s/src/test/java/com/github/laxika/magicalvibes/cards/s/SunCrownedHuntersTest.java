package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.j.JaceCunningCastaway;
import com.github.laxika.magicalvibes.cards.m.MarkOfTheVampire;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunCrownedHunters.class, Shock.class, FugitiveWizard.class,
        JaceCunningCastaway.class, MarkOfTheVampire.class})
class SunCrownedHuntersTest extends BaseCardTest {

    // ===== Non-combat damage trigger =====

    @Test
    @DisplayName("When dealt non-lethal spell damage, deals 3 damage to opponent")
    void spellDamageDeals3ToOpponent() {
        harness.addToBattlefield(player2, new SunCrownedHunters());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        UUID huntersId = harness.getPermanentId(player2, "Sun-Crowned Hunters");
        harness.castAndResolveInstant(player1, 0, huntersId);

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
        harness.castAndResolveInstant(player1, 0, huntersId);
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
        harness.castAndResolveInstant(player1, 0, huntersId);

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
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
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
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

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
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
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
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

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
        harness.castAndResolveInstant(player1, 0, huntersId);
        harness.passBothPriorities(); // Resolve first trigger (3 to opponent)

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);

        // Second Shock — 2 more damage (4 total on 5/4 = lethal)
        huntersId = harness.getPermanentId(player2, "Sun-Crowned Hunters");
        harness.castAndResolveInstant(player1, 0, huntersId);

        // Enrage trigger should be on the stack even though Hunters died
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // Resolve second trigger

        // Hunters should be dead
        harness.assertInGraveyard(player2, "Sun-Crowned Hunters");

        // Opponent should have taken 3 + 3 = 6 damage total from triggers
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Enrage can target an opponent's planeswalker")
    void canDamageOpponentsPlaneswalker() {
        Permanent hunters = harness.addToBattlefieldAndReturn(player2, new SunCrownedHunters());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceCunningCastaway());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, hunters.getId());
        harness.handlePermanentChosen(player2, jace.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jace, Cunning Castaway");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Enrage can target its controller's planeswalker")
    void canDamageControllersPlaneswalker() {
        Permanent hunters = harness.addToBattlefieldAndReturn(player2, new SunCrownedHunters());
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceCunningCastaway());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, hunters.getId());
        harness.handlePermanentChosen(player2, jace.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Jace, Cunning Castaway");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Enrage retains granted lifelink when a planeswalker requires target selection")
    void grantedLifelinkAppliesWithPlaneswalkerPresent() {
        Permanent hunters = harness.addToBattlefieldAndReturn(player2, new SunCrownedHunters());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceCunningCastaway());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new MarkOfTheVampire()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castEnchantment(player1, 0, hunters.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, hunters.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 23);
    }
}
