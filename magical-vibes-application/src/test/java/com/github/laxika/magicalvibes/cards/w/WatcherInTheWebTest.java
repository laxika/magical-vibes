package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.n.NiblisOfDusk;
import com.github.laxika.magicalvibes.cards.q.QuilledWolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WatcherInTheWeb.class, QuilledWolf.class, NiblisOfDusk.class})
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

    @Test
    @DisplayName("Reach allows Watcher in the Web to block eight flying creatures")
    void canBlockEightFlyingCreatures() {
        Permanent watcher = prepareCombat(8, NiblisOfDusk::new);
        int watcherIdx = gd.playerBattlefields.get(player2.getId()).indexOf(watcher);

        gs.declareBlockers(gd, player2, assignments(watcherIdx, 8));

        assertThat(watcher.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2, 3, 4, 5, 6, 7);
    }

    @Test
    @DisplayName("A summoning-sick Watcher can still block eight creatures")
    void canBlockWhileSummoningSick() {
        Permanent watcher = prepareCombat(8);
        watcher.setSummoningSick(true);
        int watcherIdx = gd.playerBattlefields.get(player2.getId()).indexOf(watcher);

        gs.declareBlockers(gd, player2, assignments(watcherIdx, 8));

        assertThat(watcher.getBlockingTargets()).hasSize(8);
    }

    @Test
    @DisplayName("Additional blocks do not allow a tapped Watcher to block")
    void cannotBlockWhileTapped() {
        Permanent watcher = prepareCombat(2);
        watcher.setTapped(true);
        int watcherIdx = gd.playerBattlefields.get(player2.getId()).indexOf(watcher);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, assignments(watcherIdx, 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
        assertThat(watcher.getBlockingTargets()).isEmpty();
    }

    @Test
    @DisplayName("Watcher does not grant additional blocks to other creatures")
    void doesNotGrantAdditionalBlocksToOtherCreatures() {
        prepareCombat(2);
        Permanent wolf = addCreatureReady(player2, new QuilledWolf());
        int wolfIdx = gd.playerBattlefields.get(player2.getId()).indexOf(wolf);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, assignments(wolfIdx, 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
        assertThat(wolf.getBlockingTargets()).isEmpty();
    }

    private Permanent prepareCombat(int attackerCount) {
        return prepareCombat(attackerCount, QuilledWolf::new);
    }

    private Permanent prepareCombat(int attackerCount, Supplier<Card> attackerFactory) {
        Permanent watcher = addCreatureReady(player2, new WatcherInTheWeb());

        for (int i = 0; i < attackerCount; i++) {
            Permanent attacker = addCreatureReady(player1, attackerFactory.get());
            attacker.setAttacking(true);
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
