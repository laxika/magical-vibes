package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LabyrinthMinotaurTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking pushes a non-targeting triggered ability at the blocked creature")
    void blockTriggerPushesOntoStack() {
        Permanent minotaur = addReadyBlocker(player2);
        Permanent attacker = addReadyAttacker(player1);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
        assertThat(entry.getSourcePermanentId()).isEqualTo(minotaur.getId());
        assertThat(entry.isNonTargeting()).isTrue();
    }

    @Test
    @DisplayName("Resolving the block trigger makes the blocked creature skip its next untap")
    void resolvingSetsSkipUntapCount() {
        addReadyBlocker(player2);
        Permanent attacker = addReadyAttacker(player1);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Trigger does nothing if the blocked creature leaves before resolution")
    void triggerDoesNothingIfAttackerRemoved() {
        addReadyBlocker(player2);
        addReadyAttacker(player1);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyBlocker(Player player) {
        Permanent perm = new Permanent(new LabyrinthMinotaur());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyAttacker(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareBlockers(List<BlockerAssignment> assignments) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, assignments);
    }
}
