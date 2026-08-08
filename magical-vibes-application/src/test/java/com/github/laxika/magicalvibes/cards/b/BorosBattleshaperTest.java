package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BorosBattleshaperTest extends BaseCardTest {

    @Test
    @DisplayName("First target controlled by the active player is forced to attack")
    void firstTargetOfActivePlayerMustAttack() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new BorosBattleshaper());

        advanceToCombat(player1);
        chooseTarget(bears.getId());
        decline();
        harness.passBothPriorities();

        assertThat(bears.isMustAttackThisTurn()).isTrue();
        assertThat(bears.isMustBlockThisTurnIfAble()).isFalse();
    }

    @Test
    @DisplayName("First target controlled by a defending player is forced to block instead")
    void firstTargetOfDefendingPlayerMustBlock() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new BorosBattleshaper());

        advanceToCombat(player1);
        chooseTarget(bears.getId());
        decline();
        harness.passBothPriorities();

        assertThat(bears.isMustBlockThisTurnIfAble()).isTrue();
        assertThat(bears.isMustAttackThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Second target can't block")
    void secondTargetCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new BorosBattleshaper());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player1);
        decline();
        chooseTarget(blocker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        attacker.setAttacking(true);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("Second target can't attack")
    void secondTargetCannotAttack() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new BorosBattleshaper());

        advanceToCombat(player1);
        decline();
        chooseTarget(bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(bears);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Each half applies to its own target when both are chosen")
    void bothHalvesApplyToTheirOwnTarget() {
        Permanent forced = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new BorosBattleshaper());
        Permanent locked = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player1);
        chooseTarget(forced.getId());
        chooseTarget(locked.getId());
        harness.passBothPriorities();

        assertThat(forced.isMustAttackThisTurn()).isTrue();
        assertThat(locked.isMustAttackThisTurn()).isFalse();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int lockedIndex = gd.playerBattlefields.get(player1.getId()).indexOf(locked);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(lockedIndex)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Declining both halves leaves every creature unaffected")
    void decliningBothHalvesDoesNothing() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new BorosBattleshaper());

        advanceToCombat(player1);
        decline();
        decline();
        harness.passBothPriorities();

        assertThat(bears.isMustAttackThisTurn()).isFalse();
        assertThat(bears.isMustBlockThisTurnIfAble()).isFalse();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(bears);

        assertThatCode(() -> gs.declareAttackers(gd, player1, List.of(index))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Triggers during an opponent's combat too")
    void triggersDuringOpponentCombat() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new BorosBattleshaper());

        advanceToCombat(player2);
        chooseTarget(bears.getId());
        decline();
        harness.passBothPriorities();

        assertThat(bears.isMustAttackThisTurn()).isTrue();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    /** Answers the current "up to one target creature" prompt with the given creature. */
    private void chooseTarget(java.util.UUID permanentId) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, permanentId);
    }

    /** Declines the current "up to one" prompt by answering with the controller's own player id. */
    private void decline() {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player1.getId());
    }
}
