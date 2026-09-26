package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThornElemental.class, GrizzlyBears.class})
class ThornElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Blocked Thorn Elemental can assign combat damage to defending player")
    void blockedThornElementalAssignsDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent thornElemental = addCreatureReady(player1, new ThornElemental());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareBlockers(thornElemental, blocker);

        resolveCombat();

        // Assign all 7 damage to defending player (as though unblocked)
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 7));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        // Assigning Thorn Elemental's damage as though unblocked does not stop the blocker dealing
        // its combat damage back.
        assertThat(thornElemental.getMarkedDamage()).isEqualTo(2);
        // Blocker should survive since no damage was assigned to it
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Blocked Thorn Elemental can assign combat damage to defending player")
    void blockedThornElementalAssignsDamageToDefendingPlayerUpstreamReview() {
        harness.setLife(player2, 20);
        Permanent thornElemental = addCreatureReady(player1, new ThornElemental());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareBlockers(thornElemental, blocker);
        resolveCombat();

        // Assign all 7 damage to defending player (as though unblocked)
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 7));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        // Blocker should survive since no damage was assigned to it
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Blocked Thorn Elemental can assign combat damage to blocker instead")
    void blockedThornElementalAssignsDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent thornElemental = addCreatureReady(player1, new ThornElemental());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareBlockers(thornElemental, blocker);

        resolveCombat();

        // Assign all damage to blocker instead of defending player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 7));

        // Grizzly Bears (2/2) takes 7 damage and dies
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        // Life unchanged since damage went to blocker
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Blocked Thorn Elemental can assign combat damage to blocker instead")
    void blockedThornElementalAssignsDamageToBlockerUpstreamReview() {
        harness.setLife(player2, 20);
        Permanent thornElemental = addCreatureReady(player1, new ThornElemental());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareBlockers(thornElemental, blocker);
        resolveCombat();

        // Assign all damage to blocker instead of defending player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 7));

        // Grizzly Bears (2/2) takes 7 damage -> dies
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        // Life unchanged since damage went to blocker
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Blocked Thorn Elemental cannot split damage between blocker and defending player")
    void blockedThornElementalCannotSplitDamage() {
        harness.setLife(player2, 20);
        Permanent thornElemental = addCreatureReady(player1, new ThornElemental());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareBlockers(thornElemental, blocker);

        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(
                player1, 0, Map.of(blocker.getId(), 1, player2.getId(), 6)))
                .isInstanceOf(IllegalStateException.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 7));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Blocked Thorn Elemental cannot split damage between blocker and defending player")
    void blockedThornElementalCannotSplitDamageBetweenBlockerAndDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent thornElemental = addCreatureReady(player1, new ThornElemental());
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());
        declareBlockers(thornElemental, firstBlocker, secondBlocker);
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(
                player1, 0, Map.of(player2.getId(), 1, firstBlocker.getId(), 6)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must all be assigned to the defending target");

        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 7));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Blocked Thorn Elemental can assign combat damage among multiple blockers")
    void blockedThornElementalAssignsDamageAmongMultipleBlockers() {
        harness.setLife(player2, 20);
        Permanent thornElemental = addCreatureReady(player1, new ThornElemental());
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());
        declareBlockers(thornElemental, firstBlocker, secondBlocker);

        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 2,
                secondBlocker.getId(), 5));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(firstBlocker.getId())
                        || permanent.getId().equals(secondBlocker.getId()));
    }

    private void declareBlockers(Permanent attacker, Permanent... blockers) {
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareAttackersAndPrepareBlockers(List.of(attackerIndex));
        gs.declareBlockers(gd, player2, List.of(blockers).stream()
                .map(blocker -> new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker), attackerIndex))
                .toList());
    }
}
