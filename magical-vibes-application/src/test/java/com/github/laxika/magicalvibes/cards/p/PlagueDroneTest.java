package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed(PlagueDrone.class)
class PlagueDroneTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's life gain becomes an equal amount of life loss")
    void opponentLifeGainBecomesLifeLoss() {
        harness.addToBattlefield(player1, new PlagueDrone());
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 4));

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("The controller's own life gain is unaffected")
    void controllerLifeGainIsUnaffected() {
        harness.addToBattlefield(player1, new PlagueDrone());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 4));

        harness.assertLife(player1, 24);
    }
}
