package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TransguildCourier.class, GrizzlyBears.class})
class TransguildCourierTest extends BaseCardTest {

    @Test
    @DisplayName("Transguild Courier is all five colors")
    void isAllColors() {
        Permanent courier = harness.addToBattlefieldAndReturn(player1, new TransguildCourier());

        assertThat(gqs.getEffectiveColors(gd, courier))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK,
                        CardColor.RED, CardColor.GREEN);
    }

    @Test
    @DisplayName("Transguild Courier does not affect other permanents")
    void onlyAffectsItself() {
        Permanent courier = harness.addToBattlefieldAndReturn(player1, new TransguildCourier());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectiveColors(gd, courier)).hasSize(5);
        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.GREEN);
    }
}
