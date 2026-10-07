package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({TrainedCaracal.class})
class TrainedCaracalTest extends BaseCardTest {

    @Test
    @DisplayName("Unblocked combat damage gains life for the controller")
    void gainsLifeFromUnblockedCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new TrainedCaracal());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Both attacking and blocking Caracals gain life even when they die")
    void gainsLifeFromLethalCreatureCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new TrainedCaracal());
        harness.addToBattlefield(player2, new TrainedCaracal());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 21);
        harness.assertNotOnBattlefield(player1, "Trained Caracal");
        harness.assertNotOnBattlefield(player2, "Trained Caracal");
        harness.assertInGraveyard(player1, "Trained Caracal");
        harness.assertInGraveyard(player2, "Trained Caracal");
    }
}
