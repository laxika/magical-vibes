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

    @Test
    @DisplayName("Casting Ebon Drake does not trigger its own ability")
    void doesNotTriggerForItsOwnCast() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new EbonDrake(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ebon Drake");
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("Each Ebon Drake triggers separately and before the spell resolves")
    void multipleDrakesTriggerSeparatelyBeforeSpellResolves() {
        harness.addToBattlefield(player1, new EbonDrake());
        harness.addToBattlefield(player1, new EbonDrake());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new EbonDrake(), "{2}{B}");
        harness.assertLife(player1, lifeBefore);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore - 1);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore - 2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        harness.assertLife(player1, lifeBefore - 2);
    }

    @Test
    @DisplayName("Drakes controlled by different players each make their own controller lose life")
    void eachDrakeMakesItsOwnControllerLoseLife() {
        harness.addToBattlefield(player1, new EbonDrake());
        harness.addToBattlefield(player2, new EbonDrake());
        int life1Before = gd.playerLifeTotals.get(player1.getId());
        int life2Before = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player1, new EbonDrake(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, life1Before - 1);
        harness.assertLife(player2, life2Before - 1);
    }
}
