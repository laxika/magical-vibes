package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OculusTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Oculus has correct death trigger effect")
    void hasCorrectProperties() {
        Oculus card = new Oculus();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst()).isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_DEATH).getFirst();
        assertThat(may.wrapped()).isInstanceOf(DrawCardEffect.class);
        assertThat(may.prompt()).isEqualTo("Draw a card?");
    }

    // ===== Death trigger: combat (blocker dies) =====

    @Test
    @DisplayName("Oculus dies blocking a bigger creature, accept may ability, draws a card")
    void diesInCombatAsBlockerAcceptDraw() {
        Oculus oculus = new Oculus();
        Permanent oculusPerm = new Permanent(oculus);
        oculusPerm.setSummoningSick(false);
        oculusPerm.setBlocking(true);
        oculusPerm.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(oculusPerm);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Oculus (1/1) should be dead after blocking a 2/2
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Oculus"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Oculus"));

        // Player1 should be prompted for the may ability
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Oculus"));

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Player1 should have drawn a card
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Oculus dies blocking a bigger creature, decline may ability, no card drawn")
    void diesInCombatAsBlockerDeclineDraw() {
        Oculus oculus = new Oculus();
        Permanent oculusPerm = new Permanent(oculus);
        oculusPerm.setSummoningSick(false);
        oculusPerm.setBlocking(true);
        oculusPerm.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(oculusPerm);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Oculus"));

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        // No triggered ability on the stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Oculus"));

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    // ===== Death trigger: Wrath of God =====

    @Test
    @DisplayName("Oculus dies from Wrath of God, accept may ability, draws a card")
    void diesFromWrathOfGodAcceptDraw() {
        harness.addToBattlefield(player1, new Oculus());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Oculus"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Oculus"));

        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        harness.passBothPriorities();

        // Hand should be empty (Wrath went to graveyard) + 1 drawn card
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore - 1 + 1);
    }

    @Test
    @DisplayName("Oculus dies from Wrath of God, decline may ability, no card drawn")
    void diesFromWrathOfGodDeclineDraw() {
        harness.addToBattlefield(player1, new Oculus());

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
}
