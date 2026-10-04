package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HardyVeteran;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinTrailblazer.class, HardyVeteran.class})
class GoblinTrailblazerTest extends BaseCardTest {

    @Test
    @DisplayName("Goblin Trailblazer cannot be blocked by one creature")
    void cannotBeBlockedByOneCreature() {
        Permanent trailblazer = addCreatureReady(player1, new GoblinTrailblazer());
        Permanent blocker = addCreatureReady(player2, new HardyVeteran());

        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trailblazer);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Goblin Trailblazer can be blocked by two creatures")
    void canBeBlockedByTwoCreatures() {
        Permanent trailblazer = addCreatureReady(player1, new GoblinTrailblazer());
        Permanent firstBlocker = addCreatureReady(player2, new HardyVeteran());
        Permanent secondBlocker = addCreatureReady(player2, new HardyVeteran());

        declareAttackersAndPrepareBlockers(List.of(0));

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trailblazer);
        int firstBlockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(firstBlocker);
        int secondBlockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(secondBlocker);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(firstBlockerIndex, attackerIndex),
                new BlockerAssignment(secondBlockerIndex, attackerIndex)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Goblin Trailblazer can be left unblocked even when a blocker is available")
    void canBeLeftUnblocked() {
        addCreatureReady(player1, new GoblinTrailblazer());
        Permanent blocker = addCreatureReady(player2, new HardyVeteran());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 18);
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Goblin Trailblazer can be blocked by more than two creatures")
    void canBeBlockedByThreeCreatures() {
        addCreatureReady(player1, new GoblinTrailblazer());
        Permanent firstBlocker = addCreatureReady(player2, new HardyVeteran());
        Permanent secondBlocker = addCreatureReady(player2, new HardyVeteran());
        Permanent thirdBlocker = addCreatureReady(player2, new HardyVeteran());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
        assertThat(thirdBlocker.isBlocking()).isTrue();
    }
}
