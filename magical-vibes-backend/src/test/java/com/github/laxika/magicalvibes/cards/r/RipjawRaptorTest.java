package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RipjawRaptorTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Ripjaw Raptor has one ON_DEALT_DAMAGE effect of type DrawCardEffect")
    void hasCorrectEffect() {
        RipjawRaptor card = new RipjawRaptor();

        assertThat(card.getEffects(EffectSlot.ON_DEALT_DAMAGE)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEALT_DAMAGE).get(0))
                .isInstanceOf(DrawCardEffect.class);
    }

    // ===== Non-combat damage trigger =====

    @Test
    @DisplayName("When dealt non-lethal spell damage, controller draws a card")
    void spellDamageDrawsCard() {
        harness.addToBattlefield(player2, new RipjawRaptor());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        UUID raptorId = harness.getPermanentId(player2, "Ripjaw Raptor");
        harness.castInstant(player1, 0, raptorId);
        harness.passBothPriorities(); // Resolve Shock — 2 damage to Raptor (non-lethal for 4/5)

        // ON_DEALT_DAMAGE trigger should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve the trigger
        harness.passBothPriorities();

        // Raptor should survive (4/5 takes 2 damage)
        harness.assertOnBattlefield(player2, "Ripjaw Raptor");

        // Controller should have drawn a card
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Opponent does not draw a card when Ripjaw Raptor is dealt damage")
    void opponentDoesNotDraw() {
        harness.addToBattlefield(player2, new RipjawRaptor());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID raptorId = harness.getPermanentId(player2, "Ripjaw Raptor");
        harness.castInstant(player1, 0, raptorId);
        harness.passBothPriorities(); // Resolve Shock

        // Measure after Shock resolves (Shock already left the hand), before trigger resolves
        int opponentHandSizeAfterCast = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities(); // Resolve trigger

        // Opponent (player1) should NOT have drawn from the trigger
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(opponentHandSizeAfterCast);
    }

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("When dealt non-lethal combat damage, enrage trigger fires and raptor survives")
    void combatDamageTriggersEnrage() {
        harness.addToBattlefield(player2, new RipjawRaptor());
        harness.addToBattlefield(player1, new FugitiveWizard()); // 1/1 attacker

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent raptor = gd.playerBattlefields.get(player2.getId()).getFirst();
        raptor.setSummoningSick(false);
        raptor.setBlocking(true);
        raptor.addBlockingTarget(0);

        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Resolve combat damage — auto-pass will resolve the trigger too
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Raptor should survive (4/5 takes 1 damage from 1/1)
        harness.assertOnBattlefield(player2, "Ripjaw Raptor");

        // Fugitive Wizard should die (1/1 takes 4 damage)
        harness.assertInGraveyard(player1, "Fugitive Wizard");

        // Controller should have drawn at least one card from the enrage trigger
        assertThat(gd.playerHands.get(player2.getId()).size()).isGreaterThan(handSizeBefore);
    }

    // ===== Multiple damage instances =====

    @Test
    @DisplayName("Each damage instance triggers a separate card draw")
    void multipleDamageInstancesDrawMultipleCards() {
        harness.addToBattlefield(player2, new RipjawRaptor());

        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        // First Shock — 2 damage (non-lethal for 4/5)
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID raptorId = harness.getPermanentId(player2, "Ripjaw Raptor");
        harness.castInstant(player1, 0, raptorId);
        harness.passBothPriorities(); // Resolve first Shock
        harness.passBothPriorities(); // Resolve first trigger

        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handSizeBefore + 1);

        // Second Shock — 2 more damage (4 total, still non-lethal for 4/5)
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        raptorId = harness.getPermanentId(player2, "Ripjaw Raptor");
        harness.castInstant(player1, 0, raptorId);
        harness.passBothPriorities(); // Resolve second Shock
        harness.passBothPriorities(); // Resolve second trigger

        // Raptor should still be alive (4/5 with 4 damage)
        harness.assertOnBattlefield(player2, "Ripjaw Raptor");

        // Controller should have drawn two cards total
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handSizeBefore + 2);
    }

    // ===== No damage, no trigger =====

    @Test
    @DisplayName("No trigger fires when Ripjaw Raptor is not dealt damage")
    void noTriggerWithoutDamage() {
        harness.addToBattlefield(player2, new RipjawRaptor());

        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        // Just pass priority without dealing damage
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // No cards drawn
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handSizeBefore);
    }
}
