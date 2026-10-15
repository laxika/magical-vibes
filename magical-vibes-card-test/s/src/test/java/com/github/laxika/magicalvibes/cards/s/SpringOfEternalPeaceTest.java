package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed(SpringOfEternalPeace.class)
class SpringOfEternalPeaceTest extends BaseCardTest {

    @Test
    @DisplayName("Spring of Eternal Peace gains 8 life for its controller")
    void gains8Life() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 17);

        harness.castFromHand(player1, new SpringOfEternalPeace(), "{3}{G}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 28);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Life is gained only on resolution, even above the starting life total")
    void gainsLifeOnlyOnResolutionAboveStartingLifeTotal() {
        harness.setLife(player1, 35);
        harness.setLife(player2, 12);

        harness.castFromHand(player1, new SpringOfEternalPeace(), "{3}{G}{G}");

        harness.assertLife(player1, 35);
        harness.assertLife(player2, 12);

        harness.passBothPriorities();

        harness.assertLife(player1, 43);
        harness.assertLife(player2, 12);
        harness.assertInGraveyard(player1, "Spring of Eternal Peace");
    }
}
