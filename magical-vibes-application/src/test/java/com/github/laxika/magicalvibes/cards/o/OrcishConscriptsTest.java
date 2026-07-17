package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrcishConscriptsTest extends BaseCardTest {

    // --- Attacking ---

    @Test
    @DisplayName("Orcish Conscripts can't attack alone")
    void cannotAttackAlone() {
        addCreatureReady(player1, new OrcishConscripts());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 2 other creatures attack");
    }

    @Test
    @DisplayName("Orcish Conscripts can't attack with only one other attacker")
    void cannotAttackWithSingleAlly() {
        addCreatureReady(player1, new OrcishConscripts());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 2 other creatures attack");
    }

    @Test
    @DisplayName("Orcish Conscripts can attack when two other creatures also attack")
    void canAttackWithTwoAllies() {
        addCreatureReady(player1, new OrcishConscripts());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatCode(() -> declareAttackers(player1, List.of(0, 1, 2))).doesNotThrowAnyException();
    }

    // --- Blocking ---

    @Test
    @DisplayName("Orcish Conscripts can't block alone")
    void cannotBlockAlone() {
        addReadyAttacker(player1);
        addCreatureReady(player2, new OrcishConscripts());
        setupDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 2 other creatures block");
    }

    @Test
    @DisplayName("Orcish Conscripts can't block with only one other blocker")
    void cannotBlockWithSingleAlly() {
        addReadyAttacker(player1);
        addCreatureReady(player2, new OrcishConscripts());
        addCreatureReady(player2, new GrizzlyBears());
        setupDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 2 other creatures block");
    }

    @Test
    @DisplayName("Orcish Conscripts can block when two other creatures also block")
    void canBlockWithTwoAllies() {
        addReadyAttacker(player1);
        addCreatureReady(player2, new OrcishConscripts());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        setupDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0), new BlockerAssignment(2, 0))))
                .doesNotThrowAnyException();
    }

    private Permanent addReadyAttacker(Player player) {
        Permanent attacker = addCreatureReady(player, new GrizzlyBears());
        attacker.setAttacking(true);
        return attacker;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
