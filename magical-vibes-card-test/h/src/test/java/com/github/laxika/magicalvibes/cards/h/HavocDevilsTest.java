package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HavocDevils.class, GreenwoodSentinel.class})
class HavocDevilsTest extends BaseCardTest {

    @Test
    @DisplayName("Unblocked Havoc Devils deals its full combat damage")
    void unblockedDealsFullCombatDamage() {
        addCreatureReady(player1, new HavocDevils());
        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to defending player")
    void trampleAssignsExcessDamageToDefendingPlayer() {
        harness.setLife(player2, 20);

        Permanent havocDevils = addCreatureReady(player1, new HavocDevils());
        havocDevils.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GreenwoodSentinel());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2
        ));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        harness.assertOnBattlefield(player1, "Havoc Devils");
    }

    @Test
    @DisplayName("Trample requires lethal damage to the blocker before damaging the player")
    void mustAssignLethalBeforeTramplingOver() {
        Permanent attacker = addCreatureReady(player1, new HavocDevils());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GreenwoodSentinel());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);

        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 2));
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
    }
}
