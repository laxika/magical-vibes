package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RootbreakerWurm.class, GiantSpider.class})
class RootbreakerWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RootbreakerWurm());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 4,
                player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Rootbreaker Wurm");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Trample allows assigning all combat damage to the blocker")
    void canAssignAllDamageToBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RootbreakerWurm());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 6));

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Rootbreaker Wurm");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Trample requires lethal damage to the blocker before assigning player damage")
    void rejectsPlayerDamageBeforeLethalBlockerDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RootbreakerWurm());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 3,
                player2.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Giant Spider");

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 4,
                player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Rootbreaker Wurm");
        harness.assertInGraveyard(player2, "Giant Spider");
    }
}
