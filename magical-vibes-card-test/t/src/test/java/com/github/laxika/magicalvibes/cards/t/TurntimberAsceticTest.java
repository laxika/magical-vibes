package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TurntimberAscetic.class)
class TurntimberAsceticTest extends BaseCardTest {

    @Test
    void entersTheBattlefieldAndGainsThreeLife() {
        harness.setLife(player1, 10);
        castTurntimberAscetic();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        harness.assertOnBattlefield(player1, "Turntimber Ascetic");
    }

    @Test
    void etbTriggerOnlyGainsLifeForItsController() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        castTurntimberAscetic();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    private void castTurntimberAscetic() {
        harness.castFromHand(player1, new TurntimberAscetic(), "{4}{G}");
    }

    @Test
    void lifeGainWaitsForTheEnterTriggerToResolve() {
        harness.setLife(player1, 10);
        castTurntimberAscetic();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Turntimber Ascetic");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }
}
