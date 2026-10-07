package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AzoriusHerald;
import com.github.laxika.magicalvibes.cards.s.ShiftingSky;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TransguildCourier.class, AzoriusHerald.class, ShiftingSky.class})
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
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new AzoriusHerald());

        assertThat(gqs.getEffectiveColors(gd, courier)).hasSize(5);
        assertThat(gqs.getEffectiveColors(gd, herald)).containsExactly(CardColor.WHITE);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Shifting Sky overrides Courier's all-colors ability regardless of entry order")
    void colorSettingOverridesAllColorsRegardlessOfEntryOrder(boolean skyEntersFirst) {
        if (!skyEntersFirst) {
            harness.castFromHand(player1, new TransguildCourier(), "{4}");
            harness.passBothPriorities();
        }

        harness.castFromHand(player1, new ShiftingSky(), "{2}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        Permanent sky = findPermanent(player1, "Shifting Sky");

        if (skyEntersFirst) {
            harness.castFromHand(player1, new TransguildCourier(), "{4}");
            harness.passBothPriorities();
        }

        Permanent courier = findPermanent(player1, "Transguild Courier");
        assertThat(gqs.getEffectiveColors(gd, courier)).containsExactly(CardColor.BLUE);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, sky));

        assertThat(gqs.getEffectiveColors(gd, courier))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK,
                        CardColor.RED, CardColor.GREEN);
    }
}
