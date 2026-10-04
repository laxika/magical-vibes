package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GlidediveDuo.class)
class GlidediveDuoTest extends BaseCardTest {

    @Test
    void enteringMakesEachOpponentLoseTwoLifeAndControllerGainTwoLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);
        harness.castFromHand(player1, new GlidediveDuo(), "{4}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    void lifeTotalsChangeOnlyWhenEnterTriggerResolves() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);
        harness.castFromHand(player1, new GlidediveDuo(), "{4}{B}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Glidedive Duo");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    void triggerResolvesForItsControllerEvenAfterSourceDies() {
        harness.setLife(player1, 15);
        harness.setLife(player2, 10);
        Permanent duo = harness.enterBattlefieldAndReturn(player2, new GlidediveDuo());
        assertThat(gd.stack).hasSize(1);

        duo.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player2, "Glidedive Duo");

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }
}
