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
        harness.passBothPriorities();
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
}
