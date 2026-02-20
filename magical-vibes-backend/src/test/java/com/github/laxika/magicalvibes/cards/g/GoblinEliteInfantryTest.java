package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinEliteInfantryTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Goblin Elite Infantry has correct card properties")
    void hasCorrectProperties() {
        GoblinEliteInfantry card = new GoblinEliteInfantry();

        assertThat(card.getName()).isEqualTo("Goblin Elite Infantry");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSubtypes()).containsExactlyInAnyOrder(CardSubtype.GOBLIN, CardSubtype.WARRIOR);
    }

    @Test
    @DisplayName("Goblin Elite Infantry has ON_BLOCK effect with BoostSelfEffect -1/-1")
    void hasOnBlockEffect() {
        GoblinEliteInfantry card = new GoblinEliteInfantry();

        assertThat(card.getEffects(EffectSlot.ON_BLOCK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_BLOCK).getFirst()).isInstanceOf(BoostSelfEffect.class);
        BoostSelfEffect effect = (BoostSelfEffect) card.getEffects(EffectSlot.ON_BLOCK).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(-1);
        assertThat(effect.toughnessBoost()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Goblin Elite Infantry has ON_BECOMES_BLOCKED effect with BoostSelfEffect -1/-1")
    void hasOnBecomesBlockedEffect() {
        GoblinEliteInfantry card = new GoblinEliteInfantry();

        assertThat(card.getEffects(EffectSlot.ON_BECOMES_BLOCKED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_BECOMES_BLOCKED).getFirst()).isInstanceOf(BoostSelfEffect.class);
        BoostSelfEffect effect = (BoostSelfEffect) card.getEffects(EffectSlot.ON_BECOMES_BLOCKED).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(-1);
        assertThat(effect.toughnessBoost()).isEqualTo(-1);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Goblin Elite Infantry puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new GoblinEliteInfantry()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Goblin Elite Infantry");
    }

    @Test
    @DisplayName("Resolving puts Goblin Elite Infantry onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new GoblinEliteInfantry()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Goblin Elite Infantry"));
    }

    // ===== Block trigger (when this creature blocks) =====

    @Test
    @DisplayName("Blocking pushes a triggered ability onto the stack")
    void blockTriggerPushesOntoStack() {
        Permanent goblinPerm = addGoblinReady(player2);

        Permanent atkPerm = new Permanent(new GrizzlyBears());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Goblin Elite Infantry");
        assertThat(entry.getSourcePermanentId()).isEqualTo(goblinPerm.getId());
    }

    @Test
    @DisplayName("Resolving block trigger gives -1/-1 until end of turn")
    void blockTriggerGivesMinusOneMinusOne() {
        Permanent goblinPerm = addGoblinReady(player2);

        Permanent atkPerm = new Permanent(new GrizzlyBears());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(goblinPerm.getPowerModifier()).isEqualTo(-1);
        assertThat(goblinPerm.getToughnessModifier()).isEqualTo(-1);
        assertThat(goblinPerm.getEffectivePower()).isEqualTo(1);
        assertThat(goblinPerm.getEffectiveToughness()).isEqualTo(1);
    }

    // ===== Becomes blocked trigger (when this creature is blocked while attacking) =====

    @Test
    @DisplayName("Becoming blocked pushes a triggered ability onto the stack")
    void becomesBlockedTriggerPushesOntoStack() {
        Permanent goblinPerm = addGoblinReady(player1);
        goblinPerm.setAttacking(true);

        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Goblin Elite Infantry")
                        && entry.getSourcePermanentId().equals(goblinPerm.getId()));
    }

    @Test
    @DisplayName("Resolving becomes-blocked trigger gives -1/-1 until end of turn")
    void becomesBlockedTriggerGivesMinusOneMinusOne() {
        Permanent goblinPerm = addGoblinReady(player1);
        goblinPerm.setAttacking(true);

        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(goblinPerm.getPowerModifier()).isEqualTo(-1);
        assertThat(goblinPerm.getToughnessModifier()).isEqualTo(-1);
        assertThat(goblinPerm.getEffectivePower()).isEqualTo(1);
        assertThat(goblinPerm.getEffectiveToughness()).isEqualTo(1);
    }

    // ===== Becomes blocked fires only once with multiple blockers =====

    @Test
    @DisplayName("Becomes-blocked trigger fires only once even with multiple blockers")
    void becomesBlockedFiresOnceWithMultipleBlockers() {
        Permanent goblinPerm = addGoblinReady(player1);
        goblinPerm.getCard().setPower(4);
        goblinPerm.getCard().setToughness(4);
        goblinPerm.setAttacking(true);

        Permanent blocker1 = new Permanent(new GrizzlyBears());
        blocker1.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker1);
        Permanent blocker2 = new Permanent(new GrizzlyBears());
        blocker2.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        // Should only have one becomes-blocked trigger for Goblin Elite Infantry
        long goblinTriggerCount = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Goblin Elite Infantry"))
                .count();
        assertThat(goblinTriggerCount).isEqualTo(1);
    }

    // ===== Both triggers fire in same combat =====

    @Test
    @DisplayName("Both block and becomes-blocked triggers fire when two Goblin Elite Infantry face each other")
    void bothTriggersFireWhenTwoGoblinsInCombat() {
        Permanent attackerGoblin = addGoblinReady(player1);
        attackerGoblin.setAttacking(true);

        Permanent blockerGoblin = addGoblinReady(player2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Both goblins should have triggers on the stack
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Goblin Elite Infantry"));

        // APNAP ordering: AP's trigger (becomes-blocked) on bottom, NAP's trigger (block) on top
        // NAP's trigger resolves first (LIFO)
        assertThat(gd.stack.get(0).getControllerId()).isEqualTo(player1.getId()); // AP (attacker)
        assertThat(gd.stack.get(1).getControllerId()).isEqualTo(player2.getId()); // NAP (blocker)
    }

    // ===== -1/-1 resets at end of turn =====

    @Test
    @DisplayName("-1/-1 modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent goblinPerm = addGoblinReady(player2);
        // Increase toughness so goblin survives combat damage from Bears
        goblinPerm.getCard().setPower(4);
        goblinPerm.getCard().setToughness(4);

        Permanent atkPerm = new Permanent(new GrizzlyBears());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Verify -1/-1 applied (4/4 becomes 3/3 effective)
        assertThat(goblinPerm.getPowerModifier()).isEqualTo(-1);
        assertThat(goblinPerm.getToughnessModifier()).isEqualTo(-1);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Modifiers should be reset
        assertThat(goblinPerm.getPowerModifier()).isEqualTo(0);
        assertThat(goblinPerm.getToughnessModifier()).isEqualTo(0);
        assertThat(goblinPerm.getEffectivePower()).isEqualTo(4);
        assertThat(goblinPerm.getEffectiveToughness()).isEqualTo(4);
    }

    // ===== Trigger fizzles if source removed =====

    @Test
    @DisplayName("Block trigger fizzles if Goblin Elite Infantry is removed before resolution")
    void blockTriggerFizzlesIfRemoved() {
        addGoblinReady(player2);

        Permanent atkPerm = new Permanent(new GrizzlyBears());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Remove goblin before trigger resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        // Stack should be empty and no crash
        assertThat(gd.stack).isEmpty();
    }

    // ===== No trigger when not in combat =====

    @Test
    @DisplayName("Goblin Elite Infantry has no ON_ATTACK trigger - only triggers when blocking or becoming blocked")
    void noTriggerWhenAttacking() {
        GoblinEliteInfantry card = new GoblinEliteInfantry();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).isEmpty();
    }

    // ===== Game log =====

    @Test
    @DisplayName("Becomes-blocked trigger generates appropriate game log entry")
    void becomesBlockedTriggerGeneratesLog() {
        Permanent goblinPerm = addGoblinReady(player1);
        goblinPerm.setAttacking(true);

        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Goblin Elite Infantry") && log.contains("becomes-blocked") && log.contains("trigger"));
    }

    @Test
    @DisplayName("Block trigger generates appropriate game log entry")
    void blockTriggerGeneratesLog() {
        addGoblinReady(player2);

        Permanent atkPerm = new Permanent(new GrizzlyBears());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Goblin Elite Infantry") && log.contains("block") && log.contains("trigger"));
    }

    // ===== Helper methods =====

    private Permanent addGoblinReady(Player player) {
        GoblinEliteInfantry card = new GoblinEliteInfantry();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

