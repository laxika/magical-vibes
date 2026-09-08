package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CullingMarkTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Culling Mark requires the target creature to block this turn if able")
    void resolvingMarksTargetToBlock() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castCullingMark(target);

        assertThat(target.isMustBlockThisTurnIfAble()).isTrue();
    }

    @Test
    @DisplayName("A marked creature cannot be left out of blockers when it can block")
    void markedCreatureMustBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castCullingMark(target);

        beginCombat(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("Declaring the marked creature as a blocker satisfies Culling Mark")
    void markedCreatureCanBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castCullingMark(target);

        beginCombat(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(target.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The must-block requirement wears off at end of turn")
    void requirementWearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castCullingMark(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isMustBlockThisTurnIfAble()).isFalse();
    }

    @Test
    @DisplayName("Culling Mark cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new CullingMark()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castCullingMark(Permanent target) {
        harness.setHand(player1, List.of(new CullingMark()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void beginCombat(Permanent attacker) {
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
