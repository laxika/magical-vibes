package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodTollHarpy.class})
@DisplayName("Blood-Toll Harpy")
class BloodTollHarpyTest extends BaseCardTest {

    @Test
    @DisplayName("ETB causes each player to lose 1 life")
    void etbCausesEachPlayerToLoseLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 7);
        harness.castFromHand(player1, new BloodTollHarpy(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(9);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(6);
        harness.assertOnBattlefield(player1, "Blood-Toll Harpy");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB life loss applies to both players at arbitrary life totals")
    void etbLifeLossAppliesAtArbitraryLifeTotals() {
        harness.setLife(player1, 1);
        harness.setLife(player2, 2);
        harness.castFromHand(player1, new BloodTollHarpy(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Life loss waits for the enter trigger to resolve")
    void lifeLossWaitsForTriggerResolution() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 7);
        harness.castFromHand(player1, new BloodTollHarpy(), "{2}{B}");

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 7);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blood-Toll Harpy");
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 7);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 9);
        harness.assertLife(player2, 6);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast under the opponent's control affects each player")
    void enteringUnderOpponentControlAffectsEachPlayer() {
        harness.setLife(player1, 8);
        harness.setLife(player2, 13);

        harness.enterBattlefieldAndReturn(player2, new BloodTollHarpy());
        harness.passBothPriorities();

        harness.assertLife(player1, 7);
        harness.assertLife(player2, 12);
        harness.assertOnBattlefield(player2, "Blood-Toll Harpy");
        assertThat(gd.stack).isEmpty();
    }
}
