package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed(VenerableMonk.class)
class VenerableMonkTest extends BaseCardTest {

    @Test
    @DisplayName("Its controller gains 2 life when it enters the battlefield")
    void gainsLifeWhenEntering() {
        harness.castFromHand(player1, new VenerableMonk(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }
}
