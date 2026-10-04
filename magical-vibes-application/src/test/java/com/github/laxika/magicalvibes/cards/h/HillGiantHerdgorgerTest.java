package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

@CardUsed(HillGiantHerdgorger.class)
class HillGiantHerdgorgerTest extends BaseCardTest {

    @Test
    void entersAndGainsThreeLife() {
        harness.castFromHand(player1, new HillGiantHerdgorger(), "{4}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    @Test
    void gainsLifeOnlyWhenEnterTriggerResolves() {
        harness.castFromHand(player1, new HillGiantHerdgorger(), "{4}{G}{G}");
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant Herdgorger");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    void enteringWithoutBeingCastGainsLifeForItsController() {
        harness.enterBattlefieldAndReturn(player2, new HillGiantHerdgorger());

        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Hill Giant Herdgorger");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 23);
    }
}
