package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvasionPlansTest extends BaseCardTest {

    @Test
    @DisplayName("The attacking player chooses blockers and every able creature must block")
    void attackingPlayerChoosesBlockersAndAllAbleCreaturesBlock() {
        harness.addToBattlefield(player1, new InvasionPlans());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        PendingInteraction.BlockerDeclaration pending = beginCombat(attacker);

        assertThat(pending.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(pending.defenderId()).isEqualTo(player2.getId());
        assertThat(pending.choosingForOpponent()).isTrue();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).contains(attacker.getId());
    }

    @Test
    @DisplayName("Tapped creatures are not required to block")
    void tappedCreaturesAreNotRequiredToBlock() {
        harness.addToBattlefield(player1, new InvasionPlans());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent tappedBlocker = addCreatureReady(player2, new GrizzlyBears());
        tappedBlocker.tap();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        PendingInteraction.BlockerDeclaration pending = beginCombat(attacker);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(pending.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(tappedBlocker.isBlocking()).isFalse();
    }

    private PendingInteraction.BlockerDeclaration beginCombat(Permanent attacker) {
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.inMutationScope(() -> harness.getCombatBlockService().handleDeclareBlockersStep(gd));
        return gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class);
    }
}
