package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AvenFogbringer;
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

@CardUsed({AvenFogbringer.class, GiantWarthog.class})
class GiantWarthogTest extends BaseCardTest {

    @Test
    @DisplayName("Giant Warthog's trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        Permanent giantWarthog = addCreatureReady(player1, new GiantWarthog());
        Permanent blocker = addCreatureReady(player2, new AvenFogbringer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 3
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(giantWarthog);
    }

    @Test
    @DisplayName("Trample can assign only lethal damage to the blocker and the remainder to the player")
    void assignsMinimumLethalDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent giantWarthog = addCreatureReady(player1, new GiantWarthog());
        Permanent blocker = addCreatureReady(player2, new AvenFogbringer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 4
        ));

        harness.assertLife(player2, 16);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(giantWarthog);
    }

    @Test
    @DisplayName("Trample permits assigning all combat damage to the blocker")
    void mayAssignAllDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent giantWarthog = addCreatureReady(player1, new GiantWarthog());
        Permanent blocker = addCreatureReady(player2, new AvenFogbringer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 5));

        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(giantWarthog);
    }

    @Test
    @DisplayName("Trample requires lethal damage to the blocker before assigning damage to the player")
    void cannotAssignPlayerDamageWithoutLethalDamageToBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GiantWarthog());
        Permanent blocker = addCreatureReady(player2, new AvenFogbringer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(player2.getId(), 5)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trample: must assign at least");
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 4
        ));
        harness.assertLife(player2, 16);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }
}
