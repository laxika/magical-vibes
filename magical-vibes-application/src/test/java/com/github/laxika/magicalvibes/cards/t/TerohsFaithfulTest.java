package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TerohsFaithful.class)
class TerohsFaithfulTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldCausesItsControllerToGainFourLife() {
        harness.setLife(player1, 12);
        harness.setLife(player2, 17);
        harness.castFromHand(player1, new TerohsFaithful(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
