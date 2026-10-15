package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CentaurPeacemaker.class})
class CentaurPeacemakerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger causes each player to gain 4 life")
    void etbEachPlayerGainsLife() {
        harness.setLife(player1, 7);
        harness.setLife(player2, 13);

        harness.castFromHand(player1, new CentaurPeacemaker(), "{1}{G}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Life gain waits until the enters trigger resolves")
    void lifeGainWaitsForTriggerResolution() {
        harness.setLife(player1, 7);
        harness.setLife(player2, 13);

        harness.castFromHand(player1, new CentaurPeacemaker(), "{1}{G}{W}");
        harness.assertLife(player1, 7);
        harness.assertLife(player2, 13);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 7);
        harness.assertLife(player2, 13);

        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast triggers life gain even if the creature dies before resolution")
    void noncastEntryTriggerSurvivesSourceDeath() {
        harness.setLife(player1, 7);
        harness.setLife(player2, 13);

        var centaur = harness.enterBattlefieldAndReturn(player2, new CentaurPeacemaker());
        assertThat(gd.stack).hasSize(1);
        centaur.setMarkedDamage(3);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(centaur);
        harness.assertLife(player1, 7);
        harness.assertLife(player2, 13);
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }
}
