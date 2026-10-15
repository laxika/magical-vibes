package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DwarvenTrader;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EbonyRhino.class, DwarvenTrader.class})
class EbonyRhinoTest extends BaseCardTest {

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessCombatDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EbonyRhino());
        Permanent blocker = addCreatureReady(player2, new DwarvenTrader());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 3));

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Dwarven Trader");
        harness.assertOnBattlefield(player1, "Ebony Rhino");
    }

    @Test
    @DisplayName("Trample assigns excess damage after lethal damage to every blocker")
    void trampleAssignsExcessDamageAfterLethalDamageToEveryBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EbonyRhino());
        Permanent firstBlocker = addCreatureReady(player2, new DwarvenTrader());
        Permanent secondBlocker = addCreatureReady(player2, new DwarvenTrader());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 1,
                secondBlocker.getId(), 1,
                player2.getId(), 2));

        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .hasSize(2);
        harness.assertOnBattlefield(player1, "Ebony Rhino");
    }

    @Test
    @DisplayName("Trample permits assigning all combat damage to a blocker")
    void tramplePermitsAssigningAllCombatDamageToBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EbonyRhino());
        Permanent blocker = addCreatureReady(player2, new DwarvenTrader());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 4));

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Dwarven Trader");
        harness.assertOnBattlefield(player1, "Ebony Rhino");
    }
}
