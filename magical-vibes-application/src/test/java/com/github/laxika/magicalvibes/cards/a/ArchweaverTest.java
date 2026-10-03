package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.t.TowerDrake;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Archweaver.class, TowerDrake.class})
class ArchweaverTest extends BaseCardTest {

    @Test
    void reachAllowsBlockingFlyingAttacker() {
        addCreatureReady(player1, new TowerDrake());
        Permanent archweaver = addCreatureReady(player2, new Archweaver());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(archweaver.isBlocking()).isTrue();
    }

    @Test
    void trampleDealsExcessDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new Archweaver());
        Permanent blocker = addCreatureReady(player2, new TowerDrake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 4
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player2, "Tower Drake");
        harness.assertOnBattlefield(player1, "Archweaver");
    }
}
