package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed(AngelOfMercy.class)
class AngelOfMercyTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, its controller gains 3 life")
    void enteringGivesItsControllerThreeLife() {
        harness.setLife(player1, 8);
        harness.setLife(player2, 17);

        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        resolveAllTriggers();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Its enter-the-battlefield ability gives life to the permanent's controller")
    void enteringUnderPlayerTwoGivesPlayerTwoLife() {
        harness.setLife(player1, 17);
        harness.setLife(player2, 8);

        harness.enterBattlefieldAndReturn(player2, new AngelOfMercy());
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 11);
    }

    @Test
    @DisplayName("Entering does not gain life until the triggered ability resolves")
    void lifeGainWaitsForTriggerResolution() {
        harness.setLife(player1, 8);
        harness.setLife(player2, 17);

        harness.enterBattlefieldAndReturn(player1, new AngelOfMercy());

        harness.assertLife(player1, 8);
        harness.assertLife(player2, 17);

        resolveAllTriggers();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Each Angel entering grants life only to its own controller")
    void multipleAngelsHaveIndependentTriggers() {
        harness.setLife(player1, 8);
        harness.setLife(player2, 17);

        harness.enterBattlefieldAndReturn(player1, new AngelOfMercy());
        harness.enterBattlefieldAndReturn(player2, new AngelOfMercy());
        resolveAllTriggers();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 20);
    }
}
