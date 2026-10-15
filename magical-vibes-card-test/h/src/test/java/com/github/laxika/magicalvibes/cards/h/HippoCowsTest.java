package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.p.PlagueBeetle;
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

@CardUsed({HippoCows.class, PlagueBeetle.class})
class HippoCowsTest extends BaseCardTest {

    @Test
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new HippoCows());
        Permanent blocker = addCreatureReady(player2, new PlagueBeetle());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 4
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    void mayAssignAllDamageToBlockerInsteadOfTrampling() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new HippoCows());
        Permanent blocker = addCreatureReady(player2, new HippoCows());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 5));

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Hippo-Cows");
        harness.assertInGraveyard(player2, "Hippo-Cows");
        harness.assertNotOnBattlefield(player1, "Hippo-Cows");
        harness.assertNotOnBattlefield(player2, "Hippo-Cows");
    }

    @Test
    void mustAssignLethalDamageToBlockerBeforeTrampling() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new HippoCows());
        Permanent blocker = addCreatureReady(player2, new HippoCows());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 3,
                player2.getId(), 2
        ))).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must assign at least 4 damage");

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Hippo-Cows");

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 4,
                player2.getId(), 1
        ));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Hippo-Cows");
        harness.assertInGraveyard(player2, "Hippo-Cows");
    }
}
