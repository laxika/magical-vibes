package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.ElvishLookout;
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

@CardUsed({GoliathBeetle.class, ElvishLookout.class})
class GoliathBeetleTest extends BaseCardTest {

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessCombatDamageToDefendingPlayer() {
        Permanent attacker = addCreatureReady(player1, new GoliathBeetle());
        Permanent blocker = addCreatureReady(player2, new ElvishLookout());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 2
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Goliath Beetle");
        harness.assertInGraveyard(player2, "Elvish Lookout");
    }

    @Test
    @DisplayName("Trample allows assigning all damage to the blocker")
    void canAssignAllDamageToBlocker() {
        Permanent attacker = addCreatureReady(player1, new GoliathBeetle());
        Permanent blocker = addCreatureReady(player2, new ElvishLookout());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Goliath Beetle");
        harness.assertInGraveyard(player2, "Elvish Lookout");
    }

    @Test
    @DisplayName("Trample requires lethal damage to the blocker before damage to the player")
    void requiresLethalDamageBeforeAssigningToPlayer() {
        Permanent attacker = addCreatureReady(player1, new GoliathBeetle());
        Permanent blocker = addCreatureReady(player2, new ElvishLookout());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(
                player1, 0, Map.of(player2.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Goliath Beetle");
        harness.assertOnBattlefield(player2, "Elvish Lookout");

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 2
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Goliath Beetle");
        harness.assertInGraveyard(player2, "Elvish Lookout");
    }
}
