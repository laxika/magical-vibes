package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({MesaUnicorn.class})
class MesaUnicornTest extends BaseCardTest {

    @Test
    void unblockedDamageGainsLifeForItsController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MesaUnicorn());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void bothControllersGainLifeWhenTheirUnicornsTrade() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MesaUnicorn());
        harness.addToBattlefield(player2, new MesaUnicorn());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 22);
        harness.assertNotOnBattlefield(player1, "Mesa Unicorn");
        harness.assertNotOnBattlefield(player2, "Mesa Unicorn");
        harness.assertInGraveyard(player1, "Mesa Unicorn");
        harness.assertInGraveyard(player2, "Mesa Unicorn");
    }

    @Test
    void opponentControlledAttackerGainsLifeForOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player2, new MesaUnicorn());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 22);
    }
}
