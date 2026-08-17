package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WatcherInTheWebTest extends BaseCardTest {

    @Test
    @DisplayName("Watcher in the Web can block eight creatures")
    void canBlockEightCreatures() {
        Permanent watcher = prepareCombat(8);
        int watcherIdx = gd.playerBattlefields.get(player2.getId()).indexOf(watcher);
        List<BlockerAssignment> assignments = assignments(watcherIdx, 8);

        assertThatCode(() -> gs.declareBlockers(gd, player2, assignments))
                .doesNotThrowAnyException();
        assertThat(watcher.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2, 3, 4, 5, 6, 7);
    }

    @Test
    @DisplayName("Watcher in the Web cannot block nine creatures")
    void cannotBlockNineCreatures() {
        Permanent watcher = prepareCombat(9);
        int watcherIdx = gd.playerBattlefields.get(player2.getId()).indexOf(watcher);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, assignments(watcherIdx, 9)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    private Permanent prepareCombat(int attackerCount) {
        Permanent watcher = new Permanent(new WatcherInTheWeb());
        watcher.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(watcher);

        for (int i = 0; i < attackerCount; i++) {
            Permanent attacker = new Permanent(new GrizzlyBears());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(attacker);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        return watcher;
    }

    private List<BlockerAssignment> assignments(int blockerIndex, int attackerCount) {
        return IntStream.range(0, attackerCount)
                .mapToObj(attackerIndex -> new BlockerAssignment(blockerIndex, attackerIndex))
                .toList();
    }
}
