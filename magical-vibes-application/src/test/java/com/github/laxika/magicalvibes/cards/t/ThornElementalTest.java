package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.w.WildColos;
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

@CardUsed({ThornElemental.class, WildColos.class})
class ThornElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Blocked Thorn Elemental can assign combat damage to defending player")
    void blockedThornElementalAssignsDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent thornElemental = addCreatureReady(player1, new ThornElemental());
        thornElemental.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new WildColos());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(thornElemental))));
        resolveCombat();

        // Assign all 7 damage to defending player (as though unblocked)
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 7));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        // Blocker should survive since no damage was assigned to it
        harness.assertOnBattlefield(player2, "Wild Colos");
    }

    @Test
    @DisplayName("Blocked Thorn Elemental can assign combat damage to blocker instead")
    void blockedThornElementalAssignsDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent thornElemental = addCreatureReady(player1, new ThornElemental());
        thornElemental.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new WildColos());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(thornElemental))));
        resolveCombat();

        // Assign all damage to blocker instead of defending player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 7));

        // Wild Colos (2/2) takes 7 damage -> dies
        harness.assertNotOnBattlefield(player2, "Wild Colos");
        harness.assertInGraveyard(player2, "Wild Colos");
        // Life unchanged since damage went to blocker
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Blocked Thorn Elemental cannot split damage between blocker and defending player")
    void blockedThornElementalCannotSplitDamageBetweenBlockerAndDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent thornElemental = addCreatureReady(player1, new ThornElemental());
        thornElemental.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new WildColos());
        Permanent secondBlocker = addCreatureReady(player2, new WildColos());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(firstBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(thornElemental)),
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(secondBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(thornElemental))));
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(
                player1, 0, Map.of(player2.getId(), 1, firstBlocker.getId(), 6)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must all be assigned to the defending target");

        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 7));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }
}
