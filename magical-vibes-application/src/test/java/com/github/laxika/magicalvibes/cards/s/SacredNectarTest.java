package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed(SacredNectar.class)
class SacredNectarTest extends BaseCardTest {

    @Test
    @DisplayName("Sacred Nectar gains 4 life for its controller")
    void gains4Life() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new SacredNectar(), "{1}{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Sacred Nectar does not change its opponent's life")
    void onlyControllerGainsLife() {
        harness.setLife(player1, 13);
        harness.setLife(player2, 7);
        harness.castFromHand(player1, new SacredNectar(), "{1}{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 7);
    }
}
