package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElephantRat.class})
class ElephantRatTest extends BaseCardTest {

    @Test
    @DisplayName("Menace cannot be blocked by only one creature")
    void menaceRequiresTwoBlockers() {
        Permanent attacker = addCreatureReady(player1, new ElephantRat());
        Permanent blocker = addCreatureReady(player2, new ElephantRat());
        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
    }

    @Test
    @DisplayName("Menace can be blocked by two creatures")
    void menaceCanBeBlockedByTwoBlockers() {
        Permanent attacker = addCreatureReady(player1, new ElephantRat());
        Permanent firstBlocker = addCreatureReady(player2, new ElephantRat());
        Permanent secondBlocker = addCreatureReady(player2, new ElephantRat());
        declareAttackersAndPrepareBlockers(List.of(0));

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int firstBlockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(firstBlocker);
        int secondBlockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(secondBlocker);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(firstBlockerIndex, attackerIndex),
                new BlockerAssignment(secondBlockerIndex, attackerIndex)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Menace allows the defender to choose no blockers")
    void menaceCanRemainUnblocked() {
        addCreatureReady(player1, new ElephantRat());
        addCreatureReady(player2, new ElephantRat());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Menace can be blocked by more than two creatures")
    void menaceCanBeBlockedByThreeBlockers() {
        addCreatureReady(player1, new ElephantRat());
        Permanent firstBlocker = addCreatureReady(player2, new ElephantRat());
        Permanent secondBlocker = addCreatureReady(player2, new ElephantRat());
        Permanent thirdBlocker = addCreatureReady(player2, new ElephantRat());
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
