package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChandrasSpitfire.class, Shock.class})
class ChandrasSpitfireTest extends BaseCardTest {

    // ===== Triggering =====

    @Test
    @DisplayName("Triggers when opponent is dealt noncombat damage by a spell")
    void triggersOnNoncombatDamageToOpponent() {
        harness.addToBattlefield(player1, new ChandrasSpitfire());

        // Shock targeting player2 (noncombat damage)
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();

        // Chandra's Spitfire's triggered ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getControllerId()).isEqualTo(player1.getId());
        assertThat(trigger.getSourcePermanentId()).isEqualTo(harness.getPermanentId(player1, "Chandra's Spitfire"));
    }

    @Test
    @DisplayName("Resolving the trigger gives Chandra's Spitfire +3/+0 until end of turn")
    void resolvingTriggerBoostsSpitfire() {
        harness.addToBattlefield(player1, new ChandrasSpitfire());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Spitfire trigger

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();

        Permanent spitfire = findPermanent(player1, "Chandra's Spitfire");
        assertThat(spitfire.getPowerModifier()).isEqualTo(3);
        assertThat(spitfire.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not trigger when controller is dealt noncombat damage")
    void doesNotTriggerOnDamageToController() {
        harness.addToBattlefield(player1, new ChandrasSpitfire());

        // Player2 Shocks player1 (controller of Spitfire) — should NOT trigger
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, player1.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple noncombat damage events trigger multiple times")
    void multipleNoncombatDamageEventsStack() {
        harness.addToBattlefield(player1, new ChandrasSpitfire());

        // First Shock to player2
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Spitfire trigger

        // Second Shock to player2
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Spitfire trigger

        Permanent spitfire = findPermanent(player1, "Chandra's Spitfire");

        // +3/+0 twice = +6/+0
        assertThat(spitfire.getPowerModifier()).isEqualTo(6);
        assertThat(spitfire.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not trigger when noncombat damage is dealt to a creature, not a player")
    void doesNotTriggerOnDamageToCreature() {
        harness.addToBattlefield(player1, new ChandrasSpitfire());
        harness.addToBattlefield(player2, new ChandrasSpitfire()); // just a target creature

        java.util.UUID targetCreatureId = harness.getPermanentId(player2, "Chandra's Spitfire");

        // Shock targeting opponent's creature (not the player)
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, targetCreatureId);

        GameData gd = harness.getGameData();
        // No triggered ability for Spitfire — damage was to a creature, not a player
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent's own spell dealing damage to them still triggers the boost")
    void triggersOnOpponentsOwnDamageSource() {
        Permanent spitfire = harness.addToBattlefieldAndReturn(player1, new ChandrasSpitfire());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(spitfire.getPowerModifier()).isEqualTo(3);
        assertThat(spitfire.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Combat damage to the opponent does not trigger the boost")
    void doesNotTriggerOnCombatDamage() {
        Permanent spitfire = harness.addToBattlefieldAndReturn(player1, new ChandrasSpitfire());
        harness.setLife(player2, 20);
        spitfire.setSummoningSick(false);
        spitfire.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
        assertThat(spitfire.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The boost persists through the end step and expires before the next turn")
    void boostExpiresAtEndOfTurn() {
        Permanent spitfire = harness.addToBattlefieldAndReturn(player1, new ChandrasSpitfire());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(spitfire.getPowerModifier()).isEqualTo(3);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(spitfire.getPowerModifier()).isZero();
        assertThat(spitfire.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each Spitfire gets its own boost from one damage event")
    void eachSpitfireTriggersIndependently() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ChandrasSpitfire());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ChandrasSpitfire());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isEqualTo(3);
        assertThat(second.getPowerModifier()).isEqualTo(3);
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
    }
}
