package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StomperCub.class, NestInvader.class})
class StomperCubTest extends BaseCardTest {

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessCombatDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new StomperCub());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 3
        ));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Trample requires lethal damage to every blocker before damaging the player")
    void requiresLethalDamageToEveryBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new StomperCub());
        Permanent firstBlocker = addCreatureReady(player2, new NestInvader());
        Permanent secondBlocker = addCreatureReady(player2, new NestInvader());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 2,
                secondBlocker.getId(), 1,
                player2.getId(), 2
        ))).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trample: must assign at least 2");
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 2,
                secondBlocker.getId(), 2,
                player2.getId(), 1
        ));

        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Stomper Cub");
    }

    @Test
    @DisplayName("Trample permits assigning all combat damage to the blocker")
    void mayAssignAllDamageToBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new StomperCub());
        Permanent blocker = addCreatureReady(player2, new NestInvader());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 5));

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Nest Invader");
        harness.assertOnBattlefield(player1, "Stomper Cub");
    }
}
