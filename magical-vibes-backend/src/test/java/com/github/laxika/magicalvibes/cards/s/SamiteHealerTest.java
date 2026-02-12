package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SamiteHealerTest {

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
    @DisplayName("Samite Healer has correct card properties")
    void hasCorrectProperties() {
        SamiteHealer card = new SamiteHealer();

        assertThat(card.getName()).isEqualTo("Samite Healer");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.CLERIC);
        assertThat(card.getPower()).isEqualTo(1);
        assertThat(card.getToughness()).isEqualTo(1);
        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.TAP_ACTIVATED_ABILITY)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.TAP_ACTIVATED_ABILITY).getFirst()).isInstanceOf(PreventNextDamageEffect.class);
    }

    // ===== Activation =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        addReadyHealer(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Samite Healer");
    }

    @Test
    @DisplayName("Activating ability taps the Healer")
    void activatingTapsHealer() {
        Permanent healer = addReadyHealer(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        assertThat(healer.isTapped()).isTrue();
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving ability sets global damage prevention shield")
    void resolvingSetsGlobalShield() {
        addReadyHealer(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.globalDamagePreventionShield).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolving ability logs prevention message")
    void resolvingLogsPrevention() {
        addReadyHealer(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("damage") && log.contains("prevented"));
    }

    // ===== Prevention on creature =====

    @Test
    @DisplayName("Global shield prevents 1 combat damage to a creature")
    void globalShieldPreventsCombatDamageToCreature() {
        harness.getGameData().globalDamagePreventionShield = 1;

        // Attacker: Grizzly Bears (2/2) — shields are applied to attackers first
        GrizzlyBears bear1 = new GrizzlyBears();
        Permanent attacker = new Permanent(bear1);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        // Blocker: Grizzly Bears (2/2)
        GrizzlyBears bear2 = new GrizzlyBears();
        Permanent blocker = new Permanent(bear2);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.getGameService().passPriority(harness.getGameData(), player1);
        harness.getGameService().passPriority(harness.getGameData(), player2);

        GameData gd = harness.getGameData();
        // Attacker takes 2 damage, global shield prevents 1 → 1 effective damage < 2 toughness → survives
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Blocker takes 2 damage, no shield remaining → 2 >= 2 toughness → dies
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Global shield is consumed after preventing creature damage")
    void globalShieldConsumedAfterCreatureDamage() {
        harness.getGameData().globalDamagePreventionShield = 1;

        GrizzlyBears bear1 = new GrizzlyBears();
        Permanent attacker = new Permanent(bear1);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        GrizzlyBears bear2 = new GrizzlyBears();
        Permanent blocker = new Permanent(bear2);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.getGameService().passPriority(harness.getGameData(), player1);
        harness.getGameService().passPriority(harness.getGameData(), player2);

        assertThat(harness.getGameData().globalDamagePreventionShield).isEqualTo(0);
    }

    // ===== Prevention on player =====

    @Test
    @DisplayName("Global shield prevents 1 combat damage to a player")
    void globalShieldPreventsCombatDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.getGameData().globalDamagePreventionShield = 1;

        GrizzlyBears bear = new GrizzlyBears();
        Permanent attacker = new Permanent(bear);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.getGameService().passPriority(harness.getGameData(), player1);
        harness.getGameService().passPriority(harness.getGameData(), player2);

        GameData gd = harness.getGameData();
        // 2 damage - 1 prevented = 1 effective damage → 20 - 1 = 19
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.globalDamagePreventionShield).isEqualTo(0);
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Global prevention shield is cleared at end of turn")
    void globalShieldClearedAtEndOfTurn() {
        harness.getGameData().globalDamagePreventionShield = 1;

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(harness.getGameData().globalDamagePreventionShield).isEqualTo(0);
    }

    // ===== Full integration =====

    @Test
    @DisplayName("Samite Healer activation prevents next 1 combat damage to player")
    void fullIntegrationPreventsPlayerDamage() {
        addReadyHealer(player2);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Player2 activates healer
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        // Set up combat: player1 attacks with Grizzly Bears (2/2)
        GrizzlyBears bear = new GrizzlyBears();
        Permanent attacker = new Permanent(bear);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.getGameService().passPriority(harness.getGameData(), player1);
        harness.getGameService().passPriority(harness.getGameData(), player2);

        GameData gd = harness.getGameData();
        // 2 damage - 1 global prevention = 1 effective → 20 - 1 = 19
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.globalDamagePreventionShield).isEqualTo(0);
    }

    // ===== Helpers =====

    private Permanent addReadyHealer(Player player) {
        SamiteHealer card = new SamiteHealer();
        Permanent healer = new Permanent(card);
        healer.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(healer);
        return healer;
    }
}
