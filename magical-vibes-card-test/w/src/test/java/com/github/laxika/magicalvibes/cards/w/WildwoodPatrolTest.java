package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.t.TreetopWarden;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WildwoodPatrol.class, TreetopWarden.class})
class WildwoodPatrolTest extends BaseCardTest {

    @Test
    void trampleAssignsExcessCombatDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent patrol = addCreatureReady(player1, new WildwoodPatrol());
        patrol.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new TreetopWarden());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2));

        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    void cannotTrampleOverBeforeAssigningLethalDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent patrol = addCreatureReady(player1, new WildwoodPatrol());
        patrol.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new TreetopWarden());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2));
        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    void mayAssignAllCombatDamageToBlockerWithoutTramplingOver() {
        harness.setLife(player2, 20);
        Permanent patrol = addCreatureReady(player1, new WildwoodPatrol());
        patrol.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new TreetopWarden());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 4));

        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(patrol);
    }
}
