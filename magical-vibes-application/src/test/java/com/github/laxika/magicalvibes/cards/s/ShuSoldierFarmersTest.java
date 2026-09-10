package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ShuSoldierFarmers.class)
class ShuSoldierFarmersTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger causes controller to gain 4 life")
    void etbGainsLife() {
        castShuSoldierFarmers();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB gain life works with non-default life totals")
    void etbGainsLifeWithCustomTotals() {
        harness.setLife(player1, 10);

        castShuSoldierFarmers();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    private void castShuSoldierFarmers() {
        harness.castFromHand(player1, new ShuSoldierFarmers(), "{4}{W}");
    }
}
