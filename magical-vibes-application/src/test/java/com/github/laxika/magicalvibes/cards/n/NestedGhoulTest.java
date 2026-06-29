package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NestedGhoulTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Nested Ghoul has one ON_DEALT_DAMAGE token creation effect")
    void hasCorrectEffect() {
        NestedGhoul card = new NestedGhoul();

        assertThat(card.getEffects(EffectSlot.ON_DEALT_DAMAGE)).hasSize(1);

        CreateTokenEffect effect = (CreateTokenEffect) card.getEffects(EffectSlot.ON_DEALT_DAMAGE).get(0);
        assertThat(effect.amount()).isEqualTo(1);
        assertThat(effect.tokenName()).isEqualTo("Zombie");
        assertThat(effect.power()).isEqualTo(2);
        assertThat(effect.toughness()).isEqualTo(2);
        assertThat(effect.color()).isEqualTo(CardColor.BLACK);
        assertThat(effect.subtypes()).containsExactly(CardSubtype.PHYREXIAN, CardSubtype.ZOMBIE);
        assertThat(effect.keywords()).isEmpty();
        assertThat(effect.additionalTypes()).isEmpty();
    }

    // ===== Non-combat damage trigger =====

    @Test
    @DisplayName("When Nested Ghoul is dealt lethal damage by a spell, a Zombie token is still created")
    void spellDamageCreatesToken() {
        harness.addToBattlefield(player2, new NestedGhoul());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID ghoulId = harness.getPermanentId(player2, "Nested Ghoul");
        harness.castInstant(player1, 0, ghoulId);
        harness.passBothPriorities(); // Resolve Shock — 2 damage to Nested Ghoul (lethal for 4/2)

        GameData gd = harness.getGameData();

        // ON_DEALT_DAMAGE trigger should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve the trigger
        harness.passBothPriorities();

        // Nested Ghoul should be in the graveyard (2 damage is lethal for toughness 2)
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Nested Ghoul"));

        // A 2/2 Zombie token should be on player2's battlefield
        List<Permanent> tokens = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getCard().getPower()).isEqualTo(2);
        assertThat(tokens.get(0).getCard().getToughness()).isEqualTo(2);
        assertThat(tokens.get(0).getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(tokens.get(0).getCard().isToken()).isTrue();
    }

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("When Nested Ghoul is dealt non-lethal combat damage, a Zombie token is created and it survives")
    void nonLethalCombatDamageCreatesToken() {
        harness.addToBattlefield(player2, new NestedGhoul());
        harness.addToBattlefield(player1, new FugitiveWizard()); // 1/1

        // Player1 attacks with Fugitive Wizard (1/1), player2 blocks with Nested Ghoul (4/2)
        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent ghoul = gd.playerBattlefields.get(player2.getId()).getFirst();
        ghoul.setSummoningSick(false);
        ghoul.setBlocking(true);
        ghoul.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Resolve combat damage and all triggers
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        // A Zombie token should be on player2's battlefield
        List<Permanent> tokens = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .toList();
        assertThat(tokens).hasSize(1);

        // Nested Ghoul should survive (4/2 takes 1 damage from Fugitive Wizard 1/1)
        harness.assertOnBattlefield(player2, "Nested Ghoul");

        // Fugitive Wizard should die (1/1 takes 4 damage from Nested Ghoul 4/2)
        harness.assertInGraveyard(player1, "Fugitive Wizard");
    }

    @Test
    @DisplayName("When Nested Ghoul takes lethal combat damage, a Zombie token is still created")
    void lethalCombatDamageCreatesToken() {
        harness.addToBattlefield(player2, new NestedGhoul());
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2

        // Player1 attacks with Grizzly Bears (2/2), player2 blocks with Nested Ghoul (4/2)
        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent ghoul = gd.playerBattlefields.get(player2.getId()).getFirst();
        ghoul.setSummoningSick(false);
        ghoul.setBlocking(true);
        ghoul.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Resolve combat damage and all triggers
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Nested Ghoul should be in the graveyard (4/2 takes 2 lethal damage)
        harness.assertInGraveyard(player2, "Nested Ghoul");

        // Grizzly Bears should die (2/2 takes 4 damage from Nested Ghoul 4/2)
        harness.assertInGraveyard(player1, "Grizzly Bears");

        // A Zombie token should still be on player2's battlefield
        List<Permanent> tokens = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .toList();
        assertThat(tokens).hasSize(1);
    }

    @Test
    @DisplayName("Two blockers dealing damage to Nested Ghoul create two tokens")
    void twoBlockersCreateTwoTokens() {
        harness.addToBattlefield(player1, new NestedGhoul());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Player1 attacks with Nested Ghoul, player2 blocks with two Grizzly Bears
        Permanent ghoul = gd.playerBattlefields.get(player1.getId()).getFirst();
        ghoul.setSummoningSick(false);
        ghoul.setAttacking(true);

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

        // Assign Nested Ghoul's 4 damage: 2 to each Grizzly Bears (lethal for each 2/2)
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker1.getId(), 2,
                blocker2.getId(), 2
        ));

        // Resolve combat damage and all triggers
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Two Zombie tokens should be on player1's battlefield (one per blocker source)
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .toList();
        assertThat(tokens).hasSize(2);

        // Nested Ghoul should be in the graveyard (4/2 takes 4 total combat damage)
        harness.assertInGraveyard(player1, "Nested Ghoul");
    }
}
