package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

@CardUsed({SiegeRhino.class})
class SiegeRhinoTest extends BaseCardTest {

    @Test
    void entersAndEachOpponentLosesLifeWhileControllerGainsLife() {
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new SiegeRhino(), "{1}{W}{B}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 17);
    }

    @Test
    void lifeChangesWaitForTheEnterTriggerToResolve() {
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new SiegeRhino(), "{1}{W}{B}{G}");

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 17);
    }

    @Test
    void opposingControllerGainsLifeAndTheirOpponentLosesLife() {
        harness.forceActivePlayer(player2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 15);
        harness.castFromHand(player2, new SiegeRhino(), "{1}{W}{B}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 18);
    }
}
