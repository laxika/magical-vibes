package com.github.laxika.magicalvibes.cards.w;

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

@CardUsed({Wurm.class, PlagueBeetle.class})
class WurmTest extends BaseCardTest {

    @Test
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new Wurm());
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
    void trampleMayAssignAllDamageToBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new Wurm());
        Permanent blocker = addCreatureReady(player2, new PlagueBeetle());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 5));

        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player2, "Plague Beetle");
        harness.assertOnBattlefield(player1, "Wurm");
    }

    @Test
    void trampleRequiresLethalDamageBeforeDamagingDefendingPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new Wurm());
        Permanent blocker = addCreatureReady(player2, new Wurm());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 4, player2.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 5));

        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Wurm");
        harness.assertNotOnBattlefield(player2, "Wurm");
    }
}
