package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaggotCarrier.class})
class MaggotCarrierTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger makes each player lose 1 life")
    void etbMakesEachPlayerLose1Life() {
        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player1, new MaggotCarrier(), "{B}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before - 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before - 1);
        assertThat(gd.stack).isEmpty();
    }
}
