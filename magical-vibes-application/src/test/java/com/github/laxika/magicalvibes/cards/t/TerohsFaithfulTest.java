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
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void lifeIsGainedOnlyWhenTheEnterTriggerResolves() {
        harness.setLife(player1, 12);
        harness.castFromHand(player1, new TerohsFaithful(), "{3}{W}");

        harness.assertLife(player1, 12);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Teroh's Faithful");
        harness.assertLife(player1, 12);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertLife(player1, 16);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void secondPlayersCreatureGainsLifeForTheSecondPlayer() {
        harness.setLife(player1, 12);
        harness.setLife(player2, 17);
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new TerohsFaithful(), "{3}{W}");
        resolveAllTriggers();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 21);
    }
}
