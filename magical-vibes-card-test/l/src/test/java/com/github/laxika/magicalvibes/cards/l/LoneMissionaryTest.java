package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

@CardUsed({LoneMissionary.class})
class LoneMissionaryTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldGainsFourLife() {
        harness.castFromHand(player1, new LoneMissionary(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
    }

    @Test
    void otherControllerGainsLifeOnlyWhenTriggerResolves() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new LoneMissionary(), "{1}{W}");

        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Lone Missionary");
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 24);
        harness.assertLife(player1, 20);
    }

    @Test
    void enteringWithoutBeingCastStillGainsLife() {
        harness.enterBattlefieldAndReturn(player1, new LoneMissionary());

        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }
}
