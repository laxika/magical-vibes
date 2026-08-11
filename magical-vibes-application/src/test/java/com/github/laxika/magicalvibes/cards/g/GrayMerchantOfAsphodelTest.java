package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Gray Merchant of Asphodel")
class GrayMerchantOfAsphodelTest extends BaseCardTest {

    @Test
    @DisplayName("ETB counts black devotion including itself and drains that amount")
    void etbCountsBlackDevotionAndDrainsThatAmount() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrayMerchantOfAsphodel());
        harness.setHand(player1, List.of(new GrayMerchantOfAsphodel()));
        harness.addMana(player1, ManaColor.BLACK, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        assertThat(gd.stack).isEmpty();
    }
}
