package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CentaurHealer.class})
class CentaurHealerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger causes controller to gain 3 life")
    void etbGainsLife() {
        castCentaurHealer();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Centaur Healer");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB gain life works with non-default life totals")
    void etbGainsLifeWithCustomTotals() {
        harness.setLife(player1, 7);

        castCentaurHealer();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    private void castCentaurHealer() {
        harness.castFromHand(player1, new CentaurHealer(), "{1}{G}{W}");
    }

    @Test
    @DisplayName("Life is gained only when the entry trigger resolves")
    void lifeGainWaitsForTriggerResolution() {
        castCentaurHealer();
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Centaur Healer");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast gains life for the entering creature's controller")
    void enteringWithoutCastingGainsLifeForOtherController() {
        harness.enterBattlefieldAndReturn(player2, new CentaurHealer());

        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Centaur Healer");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 23);
        assertThat(gd.stack).isEmpty();
    }
}
