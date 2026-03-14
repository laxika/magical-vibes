package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RotWolfTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Rot Wolf has correct ON_DAMAGED_CREATURE_DIES effect")
    void hasCorrectProperties() {
        RotWolf card = new RotWolf();

        assertThat(card.getEffects(EffectSlot.ON_DAMAGED_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DAMAGED_CREATURE_DIES).getFirst()).isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_DAMAGED_CREATURE_DIES).getFirst();
        assertThat(may.wrapped()).isInstanceOf(DrawCardEffect.class);
        assertThat(may.prompt()).isEqualTo("Draw a card?");
    }

    // ===== Combat: infect kills blocker, accept may =====

    @Test
    @DisplayName("Rot Wolf kills a creature via infect in combat, accept may, draws a card")
    void killsCreatureWithInfectAcceptDraw() {
        harness.addToBattlefield(player1, new RotWolf());

        // Use a 1/1 so Rot Wolf survives combat (takes only 1 damage)
        GrizzlyBears smallCreature = new GrizzlyBears();
        smallCreature.setPower(1);
        smallCreature.setToughness(1);
        harness.addToBattlefield(player2, smallCreature);

        Permanent rotWolf = gd.playerBattlefields.get(player1.getId()).getFirst();
        rotWolf.setSummoningSick(false);
        rotWolf.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Pass 1: DECLARE_BLOCKERS -> COMBAT_DAMAGE -> damage resolves -> trigger on stack
        // Pass 2: resolve triggered ability -> MayEffect prompts player
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Blocker should be dead (from -1/-1 counters via infect)
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));

        // Rot Wolf should still be alive
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Rot Wolf"));

        // May ability prompt for Rot Wolf's controller
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // CR 603.5: MayEffect resolves inline — draw happens immediately
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    // ===== Combat: infect kills blocker, decline may =====

    @Test
    @DisplayName("Rot Wolf kills a creature via infect in combat, decline may, no card drawn")
    void killsCreatureWithInfectDeclineDraw() {
        harness.addToBattlefield(player1, new RotWolf());

        // Use a 1/1 so Rot Wolf survives combat
        GrizzlyBears smallCreature = new GrizzlyBears();
        smallCreature.setPower(1);
        smallCreature.setToughness(1);
        harness.addToBattlefield(player2, smallCreature);

        Permanent rotWolf = gd.playerBattlefields.get(player1.getId()).getFirst();
        rotWolf.setSummoningSick(false);
        rotWolf.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Resolve combat and trigger
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        // No triggered ability on the stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Rot Wolf"));

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    // ===== Damaged creature dies later the same turn =====

    @Test
    @DisplayName("Creature damaged by Rot Wolf via infect dies later the same turn, triggers may draw")
    void triggersWhenDamagedCreatureDiesLaterThisTurn() {
        harness.addToBattlefield(player1, new RotWolf());

        // Create a creature tough enough to survive 2 -1/-1 counters
        GrizzlyBears toughBlocker = new GrizzlyBears();
        toughBlocker.setPower(1);
        toughBlocker.setToughness(5);
        harness.addToBattlefield(player2, toughBlocker);

        Permanent rotWolf = gd.playerBattlefields.get(player1.getId()).getFirst();
        rotWolf.setSummoningSick(false);
        rotWolf.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Resolve combat - Rot Wolf puts 2 -1/-1 counters on blocker, blocker survives (3 toughness left)
        harness.passBothPriorities();

        // Blocker should still be alive with -1/-1 counters
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(blocker.getMinusOneMinusOneCounters()).isEqualTo(2);

        // Now kill the blocker with a Cruel Edict later in the turn
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, player2.getId());
        // Pass 1: resolve Cruel Edict, creature dies, ON_DAMAGED_CREATURE_DIES trigger fires
        harness.passBothPriorities();
        // Pass 2: resolve triggered ability -> MayEffect prompts player
        harness.passBothPriorities();

        // Blocker should be dead
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // May ability prompt for Rot Wolf's controller (creature dealt damage by Rot Wolf died)
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        // Player1 should have drawn a card
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore - 1 + 1);
    }

    // ===== No trigger when undamaged creature dies =====

    @Test
    @DisplayName("Rot Wolf does not trigger when a creature it did not damage dies")
    void noTriggerWhenUndamagedCreatureDies() {
        harness.addToBattlefield(player1, new RotWolf());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Do NOT attack/block, just kill the creature with a spell
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Creature died but was not damaged by Rot Wolf - no trigger
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore - 1);
    }
}
