package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
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

@CardUsed({StampedingRhino.class, RuneclawBear.class})
class StampedingRhinoTest extends BaseCardTest {

    @Test
    void trampleDealsExcessCombatDamageToDefendingPlayer() {
        Permanent blocker = prepareBlockedCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Stampeding Rhino");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    void mustAssignLethalDamageToBlockerBeforeTramplingOver() {
        Permanent blocker = prepareBlockedCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1, player2.getId(), 3)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trample: must assign at least 2 damage");

        harness.assertLife(player2, 20);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 2));
        harness.assertLife(player2, 18);
    }

    @Test
    void mayAssignAllCombatDamageToBlocker() {
        Permanent blocker = prepareBlockedCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 4));

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Stampeding Rhino");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    private Permanent prepareBlockedCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new StampedingRhino());
        Permanent blocker = addCreatureReady(player2, new RuneclawBear());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        return blocker;
    }
}
