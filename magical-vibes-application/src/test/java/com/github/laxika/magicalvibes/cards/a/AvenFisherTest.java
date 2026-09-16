package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvenFisher.class, GrizzlyBears.class, WrathOfGod.class})
class AvenFisherTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Aven Fisher puts it on the stack and resolves to battlefield")
    void castAndResolve() {
        harness.castFromHand(player1, new AvenFisher(), "{3}{U}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Aven Fisher");
    }

    // ===== Death trigger: combat (blocker dies) =====

    @Test
    @DisplayName("Aven Fisher dies blocking a bigger creature, accept may ability, draws a card")
    void diesInCombatAsBlockerAcceptDraw() {
        // Aven Fisher (2/2) blocks a 3/3 attacker — it will die
        Permanent fisherPerm = addCreatureReady(player1, new AvenFisher());
        fisherPerm.setBlocking(true);
        fisherPerm.addBlockingTarget(0);

        // Create a 3/3 attacker for player2
        GrizzlyBears bears = new GrizzlyBears();
        bears.setPower(3);
        bears.setToughness(3);
        Permanent attacker = addCreatureReady(player2, bears);
        attacker.setAttacking(true);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        // Resolve combat damage; the following pass resolves the death trigger.
        resolveCombat(player2);

        GameData gd = harness.getGameData();

        // Aven Fisher should be dead
        harness.assertNotOnBattlefield(player1, "Aven Fisher");
        harness.assertInGraveyard(player1, "Aven Fisher");

        // Resolve MayEffect from stack → may prompt
        harness.passBothPriorities();

        // Player1 should be prompted for the may ability
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        // Accept — inner effect (DrawCardEffect) resolves inline
        harness.handleMayAbilityChosen(player1, true);

        // Player1 should have drawn a card
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Aven Fisher dies blocking a bigger creature, decline may ability, no card drawn")
    void diesInCombatAsBlockerDeclineDraw() {
        // Aven Fisher (2/2) blocks a 3/3 attacker — it will die
        Permanent fisherPerm = addCreatureReady(player1, new AvenFisher());
        fisherPerm.setBlocking(true);
        fisherPerm.addBlockingTarget(0);

        GrizzlyBears bears = new GrizzlyBears();
        bears.setPower(3);
        bears.setToughness(3);
        Permanent attacker = addCreatureReady(player2, bears);
        attacker.setAttacking(true);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        resolveCombat(player2);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Aven Fisher should be dead
        harness.assertInGraveyard(player1, "Aven Fisher");

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    // ===== Death trigger: combat (attacker dies) =====

    @Test
    @DisplayName("Aven Fisher dies as attacker blocked by bigger creature, accept may ability, draws a card")
    void diesInCombatAsAttackerAcceptDraw() {
        // Aven Fisher (2/2) attacks, blocked by a 3/3
        Permanent fisherPerm = addCreatureReady(player1, new AvenFisher());
        fisherPerm.setAttacking(true);

        GrizzlyBears bears = new GrizzlyBears();
        bears.setPower(3);
        bears.setToughness(3);
        Permanent blocker = addCreatureReady(player2, bears);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        resolveCombat(player1);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Aven Fisher should be dead
        harness.assertInGraveyard(player1, "Aven Fisher");

        // Accept the may ability — inner effect resolves inline
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        // Player1 should have drawn a card
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    // ===== Death trigger: Wrath of God =====

    @Test
    @DisplayName("Aven Fisher dies from Wrath of God, accept may ability, draws a card")
    void diesFromWrathOfGodAcceptDraw() {
        harness.addToBattlefield(player1, new AvenFisher());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        // Cast Wrath of God
        harness.castSorcery(player1, 0);

        // Resolve Wrath of God — all creatures are destroyed
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Both creatures should be dead
        harness.assertNotOnBattlefield(player1, "Aven Fisher");
        harness.assertInGraveyard(player1, "Aven Fisher");

        // Resolve MayEffect from stack → may prompt
        harness.passBothPriorities();

        // Player1 should be prompted for Aven Fisher's death trigger
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        // Accept — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        // Hand should be empty (Wrath went to graveyard) + 1 drawn card
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore - 1 + 1);
    }

    @Test
    @DisplayName("Aven Fisher dies from Wrath of God, decline may ability, no card drawn")
    void diesFromWrathOfGodDeclineDraw() {
        harness.addToBattlefield(player1, new AvenFisher());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Resolve MayEffect from stack → may prompt
        harness.passBothPriorities();

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        // No card drawn (hand size = before - 1 for casting Wrath)
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore - 1);
    }

    // ===== No trigger when Aven Fisher survives =====

    @Test
    @DisplayName("Aven Fisher survives combat, no death trigger fires")
    void survivesNoCombatDeathTrigger() {
        // Aven Fisher (2/2) blocks a 1/1 — both survive or attacker dies, Fisher lives
        Permanent fisherPerm = addCreatureReady(player1, new AvenFisher());
        fisherPerm.setBlocking(true);
        fisherPerm.addBlockingTarget(0);

        // 1/1 attacker — Aven Fisher survives
        GrizzlyBears weakAttacker = new GrizzlyBears();
        weakAttacker.setPower(1);
        weakAttacker.setToughness(1);
        Permanent attacker = addCreatureReady(player2, weakAttacker);
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Aven Fisher should still be alive
        harness.assertOnBattlefield(player1, "Aven Fisher");

        // No may ability prompt
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}

