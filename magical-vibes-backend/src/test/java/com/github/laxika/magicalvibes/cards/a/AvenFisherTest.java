package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvenFisherTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Aven Fisher has correct card properties")
    void hasCorrectProperties() {
        AvenFisher card = new AvenFisher();

        assertThat(card.getName()).isEqualTo("Aven Fisher");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{3}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.BIRD, CardSubtype.SOLDIER);
        assertThat(card.getKeywords()).containsExactly(Keyword.FLYING);
        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst()).isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_DEATH).getFirst();
        assertThat(may.wrapped()).isInstanceOf(DrawCardEffect.class);
        assertThat(may.prompt()).isEqualTo("Draw a card?");
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Aven Fisher puts it on the stack and resolves to battlefield")
    void castAndResolve() {
        harness.setHand(player1, List.of(new AvenFisher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Aven Fisher");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Aven Fisher"));
    }

    // ===== Death trigger: combat (blocker dies) =====

    @Test
    @DisplayName("Aven Fisher dies blocking a bigger creature, accept may ability, draws a card")
    void diesInCombatAsBlockerAcceptDraw() {
        // Aven Fisher (2/2) blocks a 3/3 attacker — it will die
        AvenFisher fisher = new AvenFisher();
        Permanent fisherPerm = new Permanent(fisher);
        fisherPerm.setSummoningSick(false);
        fisherPerm.setBlocking(true);
        fisherPerm.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(fisherPerm);

        // Create a 3/3 attacker for player2
        GrizzlyBears bears = new GrizzlyBears();
        bears.setPower(3);
        bears.setToughness(3);
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        // Force to combat damage step
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Both pass priority — advances to combat damage step
        harness.getGameService().passPriority(harness.getGameData(), player2);
        harness.getGameService().passPriority(harness.getGameData(), player1);

        GameData gd = harness.getGameData();

        // Aven Fisher should be dead
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Aven Fisher"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Aven Fisher"));

        // Player1 should be prompted for the may ability
        assertThat(gd.awaitingMayAbilityPlayerId).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Aven Fisher"));

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Player1 should have drawn a card
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Aven Fisher dies blocking a bigger creature, decline may ability, no card drawn")
    void diesInCombatAsBlockerDeclineDraw() {
        // Aven Fisher (2/2) blocks a 3/3 attacker — it will die
        AvenFisher fisher = new AvenFisher();
        Permanent fisherPerm = new Permanent(fisher);
        fisherPerm.setSummoningSick(false);
        fisherPerm.setBlocking(true);
        fisherPerm.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(fisherPerm);

        GrizzlyBears bears = new GrizzlyBears();
        bears.setPower(3);
        bears.setToughness(3);
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.getGameService().passPriority(harness.getGameData(), player2);
        harness.getGameService().passPriority(harness.getGameData(), player1);

        GameData gd = harness.getGameData();

        // Aven Fisher should be dead
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Aven Fisher"));

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        // No triggered ability on the stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Aven Fisher"));

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    // ===== Death trigger: combat (attacker dies) =====

    @Test
    @DisplayName("Aven Fisher dies as attacker blocked by bigger creature, accept may ability, draws a card")
    void diesInCombatAsAttackerAcceptDraw() {
        // Aven Fisher (2/2) attacks, blocked by a 3/3
        AvenFisher fisher = new AvenFisher();
        Permanent fisherPerm = new Permanent(fisher);
        fisherPerm.setSummoningSick(false);
        fisherPerm.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(fisherPerm);

        GrizzlyBears bears = new GrizzlyBears();
        bears.setPower(3);
        bears.setToughness(3);
        Permanent blocker = new Permanent(bears);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.getGameService().passPriority(harness.getGameData(), player1);
        harness.getGameService().passPriority(harness.getGameData(), player2);

        GameData gd = harness.getGameData();

        // Aven Fisher should be dead
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Aven Fisher"));

        // Accept the may ability
        assertThat(gd.awaitingMayAbilityPlayerId).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        // Resolve the triggered ability
        harness.passBothPriorities();

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
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);

        // Resolve Wrath of God — all creatures are destroyed
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Both creatures should be dead
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Aven Fisher"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Aven Fisher"));

        // Player1 should be prompted for Aven Fisher's death trigger
        assertThat(gd.awaitingMayAbilityPlayerId).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Resolve the triggered ability
        harness.passBothPriorities();

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

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

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
        AvenFisher fisher = new AvenFisher();
        Permanent fisherPerm = new Permanent(fisher);
        fisherPerm.setSummoningSick(false);
        fisherPerm.setBlocking(true);
        fisherPerm.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(fisherPerm);

        // 1/1 attacker — Aven Fisher survives
        GrizzlyBears weakAttacker = new GrizzlyBears();
        weakAttacker.setPower(1);
        weakAttacker.setToughness(1);
        Permanent attacker = new Permanent(weakAttacker);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.getGameService().passPriority(harness.getGameData(), player2);
        harness.getGameService().passPriority(harness.getGameData(), player1);

        GameData gd = harness.getGameData();

        // Aven Fisher should still be alive
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Aven Fisher"));

        // No may ability prompt
        assertThat(gd.awaitingMayAbilityPlayerId).isNull();
    }
}
