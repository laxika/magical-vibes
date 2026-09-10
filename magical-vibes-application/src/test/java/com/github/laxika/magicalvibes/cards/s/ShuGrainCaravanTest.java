package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed(ShuGrainCaravan.class)
class ShuGrainCaravanTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger causes controller to gain 2 life")
    void etbGainsLife() {
        castShuGrainCaravan();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB gain life works with non-default life totals")
    void etbGainsLifeWithCustomTotals() {
        harness.setLife(player1, 10);

        castShuGrainCaravan();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        harness.assertLife(player1, 12);
    }

    private void castShuGrainCaravan() {
        harness.castFromHand(player1, new ShuGrainCaravan(), "{2}{W}");
    }
}
