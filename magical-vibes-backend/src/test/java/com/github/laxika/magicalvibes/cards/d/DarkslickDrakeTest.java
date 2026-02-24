package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DarkslickDrakeTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Darkslick Drake has ON_DEATH DrawCardEffect")
    void hasCorrectProperties() {
        DarkslickDrake card = new DarkslickDrake();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst()).isInstanceOf(DrawCardEffect.class);
    }

    // ===== Death trigger: combat (blocker dies) =====

    @Test
    @DisplayName("Darkslick Drake dies blocking a bigger creature, draws a card")
    void diesInCombatAsBlockerDrawsCard() {
        DarkslickDrake drake = new DarkslickDrake();
        Permanent drakePerm = new Permanent(drake);
        drakePerm.setSummoningSick(false);
        drakePerm.setBlocking(true);
        drakePerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player1.getId()).add(drakePerm);

        // Create a 5/5 attacker for player2 — Drake (2/4) will die
        GrizzlyBears big = new GrizzlyBears();
        big.setPower(5);
        big.setToughness(5);
        Permanent attacker = new Permanent(big);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Both pass priority — advances to combat damage step
        harness.passBothPriorities();

        // Darkslick Drake should be dead
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Darkslick Drake"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Darkslick Drake"));

        // Triggered ability should be on the stack (mandatory, no may prompt)
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Darkslick Drake"));

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Player1 should have drawn a card
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    // ===== Death trigger: combat (attacker dies) =====

    @Test
    @DisplayName("Darkslick Drake dies as attacker blocked by bigger creature, draws a card")
    void diesInCombatAsAttackerDrawsCard() {
        DarkslickDrake drake = new DarkslickDrake();
        Permanent drakePerm = new Permanent(drake);
        drakePerm.setSummoningSick(false);
        drakePerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(drakePerm);

        // Create a 5/5 blocker for player2
        GrizzlyBears big = new GrizzlyBears();
        big.setPower(5);
        big.setToughness(5);
        Permanent blocker = new Permanent(big);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Darkslick Drake should be dead
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Darkslick Drake"));

        // Triggered ability on stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Darkslick Drake"));

        // Resolve
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    // ===== Death trigger: Wrath of God =====

    @Test
    @DisplayName("Darkslick Drake dies from Wrath of God, draws a card")
    void diesFromWrathOfGodDrawsCard() {
        harness.addToBattlefield(player1, new DarkslickDrake());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // Cast Wrath of God
        gs.playCard(gd, player1, 0, 0, null, null);

        // Resolve Wrath of God — all creatures are destroyed
        harness.passBothPriorities();

        // Darkslick Drake should be dead
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Darkslick Drake"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Darkslick Drake"));

        // Triggered ability on stack (mandatory)
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Darkslick Drake"));

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Hand should be empty (Wrath went to graveyard) + 1 drawn card
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore - 1 + 1);
    }

    // ===== No trigger when Drake survives =====

    @Test
    @DisplayName("Darkslick Drake survives combat, no death trigger fires")
    void survivesNoCombatDeathTrigger() {
        DarkslickDrake drake = new DarkslickDrake();
        Permanent drakePerm = new Permanent(drake);
        drakePerm.setSummoningSick(false);
        drakePerm.setBlocking(true);
        drakePerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player1.getId()).add(drakePerm);

        // 2/2 attacker — Drake (2/4) survives
        GrizzlyBears weakAttacker = new GrizzlyBears();
        Permanent attacker = new Permanent(weakAttacker);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Darkslick Drake should still be alive
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Darkslick Drake"));

        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Darkslick Drake"));
    }
}
