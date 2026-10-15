package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

@CardUsed({FangrenHunter.class, AlphaMyr.class})
class FangrenHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessCombatDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent blocker = addCreatureReady(player2, new AlphaMyr());
        addCreatureReady(player1, new FangrenHunter());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 3
        ));

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Alpha Myr");
    }

    @Test
    @DisplayName("Trample permits assigning all combat damage to a blocker")
    void canAssignAllDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent blocker = addCreatureReady(player2, new AlphaMyr());
        addCreatureReady(player1, new FangrenHunter());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 4));

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Alpha Myr");
        harness.assertOnBattlefield(player1, "Fangren Hunter");
    }

    @Test
    @DisplayName("Trample assigns lethal damage to each blocker before excess damage")
    void tramplesOverMultipleBlockers() {
        harness.setLife(player2, 20);
        Permanent firstBlocker = addCreatureReady(player2, new AlphaMyr());
        Permanent secondBlocker = addCreatureReady(player2, new AlphaMyr());
        addCreatureReady(player1, new FangrenHunter());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 1,
                secondBlocker.getId(), 1,
                player2.getId(), 2
        ));

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player2, "Alpha Myr");
        harness.assertInGraveyard(player2, "Alpha Myr");
        harness.assertInGraveyard(player1, "Fangren Hunter");
    }
}
