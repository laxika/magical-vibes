package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed(Nourish.class)
class NourishTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 6 life")
    void gainsSixLife() {
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new Nourish(), "{G}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
    }
}
