package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed(Sizzle.class)
class SizzleTest extends BaseCardTest {

    @Test
    @DisplayName("Sizzle deals 3 damage to the opponent and none to its controller")
    void dealsThreeToOpponent() {
        castAndResolveSizzle();

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Sizzle deals 3 damage with non-default life totals")
    void dealsThreeWithCustomTotals() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        castAndResolveSizzle();

        harness.assertLife(player2, 12);
        harness.assertLife(player1, 10);
    }

    private void castAndResolveSizzle() {
        harness.castFromHand(player1, new Sizzle(), "{2}{R}");
        harness.passBothPriorities();
    }
}
