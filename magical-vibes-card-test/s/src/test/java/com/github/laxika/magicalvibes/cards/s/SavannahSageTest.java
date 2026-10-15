package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({SavannahSage.class})
class SavannahSageTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life when it enters the battlefield")
    void gainsLifeWhenEntering() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new SavannahSage(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Life is gained only when the enters trigger resolves")
    void lifeGainWaitsForTriggerResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new SavannahSage(), "{1}{W}");

        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Savannah Sage");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Entering without being cast gains life for the entering creature's controller")
    void enteringWithoutBeingCastGainsLifeForController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new SavannahSage());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 22);
    }
}
