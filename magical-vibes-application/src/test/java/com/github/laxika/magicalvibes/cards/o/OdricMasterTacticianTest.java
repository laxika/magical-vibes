package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OdricMasterTacticianTest extends BaseCardTest {

    private Permanent addReadyCreature(java.util.UUID playerId, Permanent permanent) {
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(playerId).add(permanent);
        return permanent;
    }

    private Permanent addOdric() {
        return addReadyCreature(player1.getId(), new Permanent(new OdricMasterTactician()));
    }

    private Permanent addAlly() {
        return addReadyCreature(player1.getId(), new Permanent(new GrizzlyBears()));
    }

    private Permanent addDefender() {
        return addReadyCreature(player2.getId(), new Permanent(new GrizzlyBears()));
    }

    /** Resolves attack triggers, then advances into the declare-blockers prompt. */
    private void advanceToBlockerDeclaration() {
        resolveAllTriggers();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Odric + 3 others attacking lets the controller declare blocks")
    void odricAndThreeOthersLetsControllerDeclareBlocks() {
        Permanent odric = addOdric();
        Permanent ally1 = addAlly();
        Permanent ally2 = addAlly();
        Permanent ally3 = addAlly();
        Permanent blocker = addDefender();

        declareAttackers(List.of(0, 1, 2, 3));
        advanceToBlockerDeclaration();

        PendingInteraction.BlockerDeclaration pending =
                gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class);
        assertThat(pending).isNotNull();
        assertThat(pending.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(pending.defenderId()).isEqualTo(player2.getId());
        assertThat(pending.choosingForOpponent()).isTrue();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int odricIdx = gd.playerBattlefields.get(player1.getId()).indexOf(odric);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIdx, odricIdx)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).contains(odric.getId());
        assertThat(ally1.isAttacking()).isTrue();
        assertThat(ally2.isAttacking()).isTrue();
        assertThat(ally3.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Defender cannot declare blocks while Odric's ability is in force")
    void defenderCannotDeclareBlocks() {
        addOdric();
        addAlly();
        addAlly();
        addAlly();
        addDefender();

        declareAttackers(List.of(0, 1, 2, 3));
        advanceToBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not trigger when Odric attacks with only two other creatures")
    void doesNotTriggerWithFewerAllies() {
        addOdric();
        addAlly();
        addAlly();
        addDefender();

        declareAttackers(List.of(0, 1, 2));
        advanceToBlockerDeclaration();

        PendingInteraction.BlockerDeclaration pending =
                gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class);
        assertThat(pending).isNotNull();
        assertThat(pending.decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(pending.choosingForOpponent()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when four creatures attack but Odric does not")
    void doesNotTriggerWhenOdricDoesNotAttack() {
        addOdric();
        addAlly();
        addAlly();
        addAlly();
        addAlly();
        addDefender();

        declareAttackers(List.of(1, 2, 3, 4));
        advanceToBlockerDeclaration();

        PendingInteraction.BlockerDeclaration pending =
                gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class);
        assertThat(pending).isNotNull();
        assertThat(pending.decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(pending.choosingForOpponent()).isFalse();
    }
}
