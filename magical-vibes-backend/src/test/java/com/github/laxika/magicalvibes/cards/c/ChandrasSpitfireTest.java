package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChandrasSpitfireTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Chandra's Spitfire has the noncombat damage triggered ability")
    void hasCorrectEffects() {
        ChandrasSpitfire card = new ChandrasSpitfire();

        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE).getFirst())
                .isInstanceOf(BoostSelfEffect.class);

        BoostSelfEffect boost = (BoostSelfEffect) card.getEffects(EffectSlot.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(3);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
    }

    // ===== Triggering =====

    @Test
    @DisplayName("Triggers when opponent is dealt noncombat damage by a spell")
    void triggersOnNoncombatDamageToOpponent() {
        harness.addToBattlefield(player1, new ChandrasSpitfire());

        // Shock targeting player2 (noncombat damage)
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Shock

        GameData gd = harness.getGameData();

        // Chandra's Spitfire's triggered ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Chandra's Spitfire");
        assertThat(trigger.getEffectsToResolve()).hasSize(1);
        assertThat(trigger.getEffectsToResolve().getFirst()).isInstanceOf(BoostSelfEffect.class);
    }

    @Test
    @DisplayName("Resolving the trigger gives Chandra's Spitfire +3/+0 until end of turn")
    void resolvingTriggerBoostsSpitfire() {
        harness.addToBattlefield(player1, new ChandrasSpitfire());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Shock
        harness.passBothPriorities(); // Resolve Spitfire trigger

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();

        Permanent spitfire = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Chandra's Spitfire"))
                .findFirst().orElseThrow();
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
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities(); // Resolve Shock

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
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Shock
        harness.passBothPriorities(); // Resolve Spitfire trigger

        // Second Shock to player2
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Shock
        harness.passBothPriorities(); // Resolve Spitfire trigger

        GameData gd = harness.getGameData();
        Permanent spitfire = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Chandra's Spitfire"))
                .findFirst().orElseThrow();

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
        harness.castInstant(player1, 0, targetCreatureId);
        harness.passBothPriorities(); // Resolve Shock

        GameData gd = harness.getGameData();
        // No triggered ability for Spitfire — damage was to a creature, not a player
        assertThat(gd.stack).isEmpty();
    }
}
