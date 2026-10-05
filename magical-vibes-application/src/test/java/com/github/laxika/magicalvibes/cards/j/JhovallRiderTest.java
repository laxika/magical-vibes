package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({JhovallRider.class, FreshVolunteers.class})
class JhovallRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new JhovallRider());
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 1
        ));

        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player2, "Fresh Volunteers");
        harness.assertOnBattlefield(player1, "Jhovall Rider");
    }

    @Test
    @DisplayName("Trample requires lethal damage to the blocker before damaging the player")
    void cannotAssignOverflowBeforeLethalDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new JhovallRider());
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 2
        ))).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trample: must assign at least 2");

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Fresh Volunteers");

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 1
        ));

        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player2, "Fresh Volunteers");
    }

    @Test
    @DisplayName("Trample permits assigning all damage to the blocker")
    void mayAssignAllDamageToBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new JhovallRider());
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3));

        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player2, "Fresh Volunteers");
        harness.assertOnBattlefield(player1, "Jhovall Rider");
    }
}
