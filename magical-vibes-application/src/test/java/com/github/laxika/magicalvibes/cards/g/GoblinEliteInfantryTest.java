package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.testutil.TestCards;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinEliteInfantry.class, GrizzlyBears.class, HighGround.class})
class GoblinEliteInfantryTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Goblin Elite Infantry puts it on the stack")
    void castingPutsOnStack() {
        GoblinEliteInfantry card = new GoblinEliteInfantry();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isInstanceOf(GoblinEliteInfantry.class);
    }

    @Test
    @DisplayName("Resolving puts Goblin Elite Infantry onto the battlefield")
    void resolvingPutsOnBattlefield() {
        GoblinEliteInfantry card = new GoblinEliteInfantry();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GoblinEliteInfantry);
    }

    // ===== Block trigger (when this creature blocks) =====

    @Test
    @DisplayName("Blocking pushes a triggered ability onto the stack")
    void blockTriggerPushesOntoStack() {
        Permanent goblinPerm = addCreatureReady(player2, new GoblinEliteInfantry());

        Permanent atkPerm = addCreatureReady(player1, new GrizzlyBears());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard()).isInstanceOf(GoblinEliteInfantry.class);
        assertThat(entry.getSourcePermanentId()).isEqualTo(goblinPerm.getId());
    }

    @Test
    @DisplayName("Resolving block trigger gives -1/-1 until end of turn")
    void blockTriggerGivesMinusOneMinusOne() {
        Permanent goblinPerm = addCreatureReady(player2, new GoblinEliteInfantry());

        Permanent atkPerm = addCreatureReady(player1, new GrizzlyBears());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(goblinPerm.getPowerModifier()).isEqualTo(-1);
        assertThat(goblinPerm.getToughnessModifier()).isEqualTo(-1);
        assertThat(goblinPerm.getEffectivePower()).isEqualTo(1);
        assertThat(goblinPerm.getEffectiveToughness()).isEqualTo(1);
    }

    // ===== Becomes blocked trigger (when this creature is blocked while attacking) =====

    @Test
    @DisplayName("Becoming blocked pushes a triggered ability onto the stack")
    void becomesBlockedTriggerPushesOntoStack() {
        Permanent goblinPerm = addCreatureReady(player1, new GoblinEliteInfantry());
        goblinPerm.setAttacking(true);

        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard() instanceof GoblinEliteInfantry
                        && entry.getSourcePermanentId().equals(goblinPerm.getId()));
    }

    @Test
    @DisplayName("Resolving becomes-blocked trigger gives -1/-1 until end of turn")
    void becomesBlockedTriggerGivesMinusOneMinusOne() {
        Permanent goblinPerm = addCreatureReady(player1, new GoblinEliteInfantry());
        goblinPerm.setAttacking(true);

        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(goblinPerm.getPowerModifier()).isEqualTo(-1);
        assertThat(goblinPerm.getToughnessModifier()).isEqualTo(-1);
        assertThat(goblinPerm.getEffectivePower()).isEqualTo(1);
        assertThat(goblinPerm.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocking multiple creatures triggers only once")
    void blockTriggerFiresOnlyOnceWhenBlockingMultipleCreatures() {
        harness.addToBattlefield(player2, new HighGround());
        Permanent goblinPerm = addCreatureReady(player2, new GoblinEliteInfantry());
        Permanent attacker1 = addCreatureReady(player1, new GrizzlyBears());
        attacker1.setAttacking(true);
        Permanent attacker2 = addCreatureReady(player1, new GrizzlyBears());
        attacker2.setAttacking(true);

        prepareDeclareBlockers();
        int goblinIndex = gd.playerBattlefields.get(player2.getId()).indexOf(goblinPerm);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(goblinIndex, 0),
                new BlockerAssignment(goblinIndex, 1)));

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getCard() instanceof GoblinEliteInfantry))
                .hasSize(1);
        resolveAllTriggers();

        assertThat(goblinPerm.getPowerModifier()).isEqualTo(-1);
        assertThat(goblinPerm.getToughnessModifier()).isEqualTo(-1);
    }

    // ===== Becomes blocked fires only once with multiple blockers =====

    @Test
    @DisplayName("Becomes-blocked trigger fires only once even with multiple blockers")
    void becomesBlockedFiresOnceWithMultipleBlockers() {
        Permanent goblinPerm = addCreatureReady(player1, new GoblinEliteInfantry());
        TestCards.mutableCard(goblinPerm).setPower(4);
        TestCards.mutableCard(goblinPerm).setToughness(4);
        goblinPerm.setAttacking(true);

        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        // Should only have one becomes-blocked trigger for Goblin Elite Infantry
        long goblinTriggerCount = gd.stack.stream()
                .filter(entry -> entry.getSourcePermanentId().equals(goblinPerm.getId()))
                .count();
        assertThat(goblinTriggerCount).isEqualTo(1);
    }

    // ===== Both triggers fire in same combat =====

    @Test
    @DisplayName("Both block and becomes-blocked triggers fire when two Goblin Elite Infantry face each other")
    void bothTriggersFireWhenTwoGoblinsInCombat() {
        Permanent attackerGoblin = addCreatureReady(player1, new GoblinEliteInfantry());
        attackerGoblin.setAttacking(true);

        addCreatureReady(player2, new GoblinEliteInfantry());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Both goblins should have triggers on the stack
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard() instanceof GoblinEliteInfantry);

        // APNAP ordering: AP's trigger (becomes-blocked) on bottom, NAP's trigger (block) on top
        // NAP's trigger resolves first (LIFO)
        assertThat(gd.stack.get(0).getControllerId()).isEqualTo(player1.getId()); // AP (attacker)
        assertThat(gd.stack.get(1).getControllerId()).isEqualTo(player2.getId()); // NAP (blocker)
    }

    // ===== -1/-1 resets at end of turn =====

    @Test
    @DisplayName("-1/-1 modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent goblinPerm = addCreatureReady(player2, new GoblinEliteInfantry());
        // Increase toughness so goblin survives combat damage from Bears
        TestCards.mutableCard(goblinPerm).setPower(4);
        TestCards.mutableCard(goblinPerm).setToughness(4);

        Permanent atkPerm = addCreatureReady(player1, new GrizzlyBears());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

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
        addCreatureReady(player2, new GoblinEliteInfantry());

        Permanent atkPerm = addCreatureReady(player1, new GrizzlyBears());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Remove goblin before trigger resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        resolveAllTriggers();

        // Stack should be empty and no crash
        assertThat(gd.stack).isEmpty();
    }

    // ===== Game log =====

    @Test
    @DisplayName("Becomes-blocked trigger generates appropriate game log entry")
    void becomesBlockedTriggerGeneratesLog() {
        Permanent goblinPerm = addCreatureReady(player1, new GoblinEliteInfantry());
        goblinPerm.setAttacking(true);

        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gameLogContains("'s becomes-blocked ability triggers.")).isTrue();
    }

    @Test
    @DisplayName("Block trigger generates appropriate game log entry")
    void blockTriggerGeneratesLog() {
        addCreatureReady(player2, new GoblinEliteInfantry());

        Permanent atkPerm = addCreatureReady(player1, new GrizzlyBears());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gameLogContains("'s block ability triggers.")).isTrue();
    }
}

