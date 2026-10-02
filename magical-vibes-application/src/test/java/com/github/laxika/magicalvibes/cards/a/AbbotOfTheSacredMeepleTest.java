package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

@CardUsed(AbbotOfTheSacredMeeple.class)
class AbbotOfTheSacredMeepleTest extends BaseCardTest {

    @Test
    void canBeCastAsACreature() {
        harness.castFromHand(player1, new AbbotOfTheSacredMeeple(), "{1}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Abbot of the Sacred Meeple");
    }
}
