package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HarborSerpent.class, Island.class, RuneclawBear.class})
class HarborSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Harbor Serpent can attack when there are exactly 5 Islands on the battlefield")
    void canAttackWithFiveIslands() {
        harness.setLife(player2, 20);
        // 3 Islands on player1's side, 2 on player2's side = 5 total
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());

        Permanent serpent = addCreatureReady(player1, new HarborSerpent());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(serpent)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Harbor Serpent can attack when there are more than 5 Islands on the battlefield")
    void canAttackWithMoreThanFiveIslands() {
        harness.setLife(player2, 20);
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Island());
        }
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new Island());
        }

        Permanent serpent = addCreatureReady(player1, new HarborSerpent());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(serpent)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Harbor Serpent cannot attack when there are fewer than 5 Islands on the battlefield")
    void cannotAttackWithFewerThanFiveIslands() {
        // 2 Islands on player1, 2 on player2 = only 4 total
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());

        Permanent serpent = addCreatureReady(player1, new HarborSerpent());

        int serpentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(serpent);
        assertThatThrownBy(() -> declareAttackers(player1, List.of(serpentIndex)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Harbor Serpent cannot attack when there are no Islands on the battlefield")
    void cannotAttackWithNoIslands() {
        addCreatureReady(player1, new HarborSerpent());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Islands on both sides of the battlefield count toward the five required")
    void islandsFromBothPlayersCounted() {
        harness.setLife(player2, 20);
        // 2 on player1's side, 3 on player2's side = 5 total
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());

        Permanent serpent = addCreatureReady(player1, new HarborSerpent());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(serpent)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    void islandwalkPreventsBlockingWhenDefenderControlsIsland() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new Island());
        }
        Permanent serpent = addCreatureReady(player1, new HarborSerpent());
        Permanent blocker = addCreatureReady(player2, new RuneclawBear());
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(serpent);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        declareAttackersAndPrepareBlockers(List.of(attackerIndex));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void controllersIslandsAllowAttackButDoNotPreventBlocking() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Island());
        }
        Permanent serpent = addCreatureReady(player1, new HarborSerpent());
        Permanent blocker = addCreatureReady(player2, new RuneclawBear());
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(serpent);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        declareAttackersAndPrepareBlockers(List.of(attackerIndex));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBlockWithoutAnyIslands() {
        Permanent attacker = addCreatureReady(player2, new RuneclawBear());
        Permanent serpent = addCreatureReady(player1, new HarborSerpent());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(serpent.isBlocking()).isTrue();
        assertThat(attacker.isAttacking()).isTrue();
    }
}
