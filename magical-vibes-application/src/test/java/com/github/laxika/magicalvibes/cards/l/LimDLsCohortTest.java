package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LimDLsCohortTest extends BaseCardTest {

    @Test
    @DisplayName("When the Cohort blocks a creature, that attacker can't be regenerated this turn")
    void blocksCreatureMarksCantRegenerate() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new LimDLsCohort());

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(attacker.isCantRegenerateThisTurn()).isTrue();
    }

    @Test
    @DisplayName("When the Cohort becomes blocked by a creature, that blocker can't be regenerated this turn")
    void becomesBlockedMarksCantRegenerate() {
        Permanent cohort = addCreatureReady(player1, new LimDLsCohort());
        cohort.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(blocker.isCantRegenerateThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The can't-be-regenerated mark clears during end-of-turn cleanup")
    void markClearsAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new LimDLsCohort());

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        assertThat(attacker.isCantRegenerateThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances END -> CLEANUP

        assertThat(attacker.isCantRegenerateThisTurn()).isFalse();
    }

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void resolveAllTriggers() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
