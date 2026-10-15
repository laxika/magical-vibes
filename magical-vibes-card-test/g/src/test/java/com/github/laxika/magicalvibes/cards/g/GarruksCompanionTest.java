package com.github.laxika.magicalvibes.cards.g;

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

@CardUsed({GarruksCompanion.class, RuneclawBear.class})
class GarruksCompanionTest extends BaseCardTest {

    @Test
    void trampleDealsExcessDamageEvenWhenAttackerDies() {
        prepareBlockedCombat();
        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Garruk's Companion");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    void cannotTrampleOverBeforeAssigningLethalDamageToBlocker() {
        prepareBlockedCombat();
        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1, player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trample: must assign at least 2 damage");

        harness.assertLife(player2, 20);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 1));
        harness.assertLife(player2, 19);
    }

    @Test
    void mayAssignAllCombatDamageToBlockerInsteadOfTramplingOver() {
        prepareBlockedCombat();
        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3));

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Garruk's Companion");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    private void prepareBlockedCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GarruksCompanion());
        addCreatureReady(player2, new RuneclawBear());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
    }
}
