package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BlisterstickShaman;
import com.github.laxika.magicalvibes.cards.b.BrassSquire;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TangleMantis.class, BlisterstickShaman.class, BrassSquire.class})
class TangleMantisTest extends BaseCardTest {

    @Test
    void trampleDealsExcessDamageToDefendingPlayer() {
        addCreatureReady(player1, new TangleMantis());
        Permanent blocker = addCreatureReady(player2, new BlisterstickShaman());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1, player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Blisterstick Shaman");
        harness.assertNotOnBattlefield(player2, "Blisterstick Shaman");
        harness.assertOnBattlefield(player1, "Tangle Mantis");
    }

    @Test
    void mustAssignLethalDamageBeforeTramplingOverBlocker() {
        addCreatureReady(player1, new TangleMantis());
        Permanent blocker = addCreatureReady(player2, new BrassSquire());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3));
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Brass Squire");
        harness.assertOnBattlefield(player1, "Tangle Mantis");
    }

    @Test
    void mayAssignAllDamageToBlockerRatherThanTrampleOver() {
        addCreatureReady(player1, new TangleMantis());
        Permanent blocker = addCreatureReady(player2, new BlisterstickShaman());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3));

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Blisterstick Shaman");
        harness.assertOnBattlefield(player1, "Tangle Mantis");
    }

    @Test
    void cannotTrampleOverBlockerWithMoreToughnessThanItsPower() {
        addCreatureReady(player1, new TangleMantis());
        Permanent blocker = addCreatureReady(player2, new TangleMantis());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3));

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Tangle Mantis");
        harness.assertOnBattlefield(player2, "Tangle Mantis");
    }
}
