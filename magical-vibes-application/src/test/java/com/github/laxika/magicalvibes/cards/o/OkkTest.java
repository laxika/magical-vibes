package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

class OkkTest extends BaseCardTest {

    // --- Attacking ---

    @Test
    @DisplayName("Okk can't attack alone")
    void cannotAttackAlone() {
        addCreatureReady(player1, new Okk());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("greater power also attacks");
    }

    @Test
    @DisplayName("Okk can't attack when the other attacker has less power")
    void cannotAttackWithWeakerAlly() {
        addCreatureReady(player1, new Okk());
        addCreatureReady(player1, new HillGiant()); // 3/3 < 4/4

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("greater power also attacks");
    }

    @Test
    @DisplayName("Okk can attack when a creature with greater power also attacks")
    void canAttackWithStrongerAlly() {
        addCreatureReady(player1, new Okk());
        addCreatureReady(player1, new AvatarOfMight()); // 8/8 > 4/4

        assertThatCode(() -> declareAttackers(player1, List.of(0, 1))).doesNotThrowAnyException();
    }

    // --- Blocking ---

    @Test
    @DisplayName("Okk can't block alone")
    void cannotBlockAlone() {
        Permanent attacker = addReadyAttacker(player1);
        addCreatureReady(player2, new Okk());
        setupDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("greater power also blocks");
    }

    @Test
    @DisplayName("Okk can't block when the other blocker has less power")
    void cannotBlockWithWeakerAlly() {
        addReadyAttacker(player1);
        addCreatureReady(player2, new Okk());
        addCreatureReady(player2, new HillGiant()); // 3/3 < 4/4
        setupDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("greater power also blocks");
    }

    @Test
    @DisplayName("Okk can block when a creature with greater power also blocks")
    void canBlockWithStrongerAlly() {
        addReadyAttacker(player1);
        addCreatureReady(player2, new Okk());
        addCreatureReady(player2, new AvatarOfMight()); // 8/8 > 4/4
        setupDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
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
