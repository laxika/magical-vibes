package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CathedralSanctifier.class})
class CathedralSanctifierTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger causes controller to gain 3 life")
    void etbGainsLife() {
        castCathedralSanctifier();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        harness.assertOnBattlefield(player1, "Cathedral Sanctifier");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB gain life works with non-default life totals")
    void etbGainsLifeWithCustomTotals() {
        harness.setLife(player1, 7);

        castCathedralSanctifier();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Life is gained only when the enters trigger resolves")
    void lifeGainWaitsForTriggerResolution() {
        castCathedralSanctifier();
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cathedral Sanctifier");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Player two gains life when they control the enters trigger")
    void playerTwoGainsLife() {
        gd.activePlayerId = player2.getId();
        harness.castFromHand(player2, new CathedralSanctifier(), "{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Cathedral Sanctifier");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 23);
        assertThat(gd.stack).isEmpty();
    }

    private void castCathedralSanctifier() {
        harness.castFromHand(player1, new CathedralSanctifier(), "{W}");
    }
}
