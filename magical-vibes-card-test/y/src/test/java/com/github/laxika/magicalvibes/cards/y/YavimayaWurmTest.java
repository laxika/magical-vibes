package com.github.laxika.magicalvibes.cards.y;

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

@CardUsed({YavimayaWurm.class, PlagueBeetle.class})
class YavimayaWurmTest extends BaseCardTest {

    @Test
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new YavimayaWurm());
        Permanent blocker = addCreatureReady(player2, new PlagueBeetle());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 5
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    void trampleRequiresLethalDamageToEveryBlockerBeforeDamagingPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new YavimayaWurm());
        Permanent firstBlocker = addCreatureReady(player2, new PlagueBeetle());
        Permanent secondBlocker = addCreatureReady(player2, new PlagueBeetle());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 1,
                player2.getId(), 5
        ))).isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 1,
                secondBlocker.getId(), 1,
                player2.getId(), 4
        ));

        harness.assertLife(player2, 16);
        harness.assertNotOnBattlefield(player2, "Plague Beetle");
        harness.assertOnBattlefield(player1, "Yavimaya Wurm");
    }

    @Test
    void trampleAllowsAssigningAllDamageToBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new YavimayaWurm());
        Permanent blocker = addCreatureReady(player2, new PlagueBeetle());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 6));

        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player2, "Plague Beetle");
        harness.assertOnBattlefield(player1, "Yavimaya Wurm");
    }
}
