package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EbonDrake.class})
class EbonDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Controller loses 1 life when any player casts a spell")
    void controllerLosesLifeWhenAnyPlayerCastsSpell() {
        harness.addToBattlefield(player1, new EbonDrake());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int drakeControllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int casterLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player2, new EbonDrake(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(drakeControllerLifeBefore - 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(casterLifeBefore);
    }

    @Test
    @DisplayName("Controller loses 1 life when they cast a spell")
    void controllerLosesLifeWhenTheyCastSpell() {
        harness.addToBattlefield(player1, new EbonDrake());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new EbonDrake(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }
}
