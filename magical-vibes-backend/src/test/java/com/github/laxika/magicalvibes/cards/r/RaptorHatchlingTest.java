package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RaptorHatchlingTest extends BaseCardTest {

    private Permanent findPermanent(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Permanent not found: " + cardName));
    }

    private Permanent findToken(Player player, String tokenName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals(tokenName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Token not found: " + tokenName));
    }

    private long countTokens(Player player, String tokenName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals(tokenName))
                .count();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Raptor Hatchling has one ON_DEALT_DAMAGE effect")
    void hasCorrectEffect() {
        RaptorHatchling card = new RaptorHatchling();

        assertThat(card.getEffects(EffectSlot.ON_DEALT_DAMAGE)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEALT_DAMAGE).get(0))
                .isInstanceOf(CreateTokenEffect.class);
    }

    // ===== Non-combat damage trigger =====

    @Test
    @DisplayName("When dealt spell damage, creates a 3/3 green Dinosaur token with trample")
    void spellDamageCreatesToken() {
        harness.addToBattlefield(player2, new RaptorHatchling());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID hatchlingId = harness.getPermanentId(player2, "Raptor Hatchling");
        harness.castInstant(player1, 0, hatchlingId);
        harness.passBothPriorities(); // Resolve Shock — 2 damage kills the 1/1

        // ON_DEALT_DAMAGE trigger should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve the trigger
        harness.passBothPriorities();

        // Hatchling dies from lethal damage but trigger still resolves
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> !p.getCard().isToken() && p.getCard().getName().equals("Raptor Hatchling"))
                .count()).isZero();

        // Token should be on the battlefield
        Permanent token = findToken(player2, "Dinosaur");
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
    }

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("When dealt combat damage, creates a 3/3 Dinosaur token")
    void combatDamageCreatesToken() {
        harness.addToBattlefield(player2, new RaptorHatchling());
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2 attacker

        Permanent attacker = findPermanent(player1, "Grizzly Bears");
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent hatchling = findPermanent(player2, "Raptor Hatchling");
        hatchling.setSummoningSick(false);
        hatchling.setBlocking(true);
        hatchling.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Resolve combat damage and trigger
        harness.passBothPriorities(); // combat damage
        harness.passBothPriorities(); // trigger on stack
        harness.passBothPriorities(); // resolve trigger

        // Token should exist
        Permanent token = findToken(player2, "Dinosaur");
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
    }

    // ===== Multiple triggers =====

    @Test
    @DisplayName("Multiple damage events create multiple tokens")
    void multipleDamageEventsCreateMultipleTokens() {
        harness.addToBattlefield(player1, new RaptorHatchling());
        // Give it enough toughness to survive two Shocks (we'll use a second card for the second hit)
        // Actually, the hatchling is 1/1 so it dies from first Shock.
        // Instead, test with two Raptor Hatchlings each getting damaged.
        harness.addToBattlefield(player1, new RaptorHatchling());

        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        // Shock first hatchling
        List<Permanent> hatchlings = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Raptor Hatchling"))
                .toList();
        harness.castInstant(player2, 0, hatchlings.get(0).getId());
        harness.passBothPriorities(); // Resolve first Shock
        harness.passBothPriorities(); // Resolve first trigger

        // Shock second hatchling
        harness.castInstant(player2, 0, hatchlings.get(1).getId());
        harness.passBothPriorities(); // Resolve second Shock
        harness.passBothPriorities(); // Resolve second trigger

        // Should have two Dinosaur tokens
        assertThat(countTokens(player1, "Dinosaur")).isEqualTo(2);
    }

    // ===== Token belongs to correct player =====

    @Test
    @DisplayName("Token is created under the Hatchling controller's control")
    void tokenBelongsToController() {
        harness.addToBattlefield(player2, new RaptorHatchling());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID hatchlingId = harness.getPermanentId(player2, "Raptor Hatchling");
        harness.castInstant(player1, 0, hatchlingId);
        harness.passBothPriorities(); // Resolve Shock
        harness.passBothPriorities(); // Resolve trigger

        // Token should be on player2's battlefield, not player1's
        assertThat(countTokens(player2, "Dinosaur")).isEqualTo(1);
        assertThat(countTokens(player1, "Dinosaur")).isZero();
    }

    // ===== Trigger still fires even when creature dies =====

    @Test
    @DisplayName("Trigger fires even when Hatchling dies from the damage")
    void triggerFiresEvenWhenCreatureDies() {
        harness.addToBattlefield(player1, new RaptorHatchling());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID hatchlingId = harness.getPermanentId(player1, "Raptor Hatchling");
        harness.castInstant(player2, 0, hatchlingId);
        harness.passBothPriorities(); // Resolve Shock — kills the 1/1

        // Trigger should be on stack
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // Resolve trigger

        // Hatchling should be dead
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> !p.getCard().isToken() && p.getCard().getName().equals("Raptor Hatchling"))
                .count()).isZero();

        // But the Dinosaur token should exist
        assertThat(countTokens(player1, "Dinosaur")).isEqualTo(1);
    }
}
