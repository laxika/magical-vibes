package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VampireLacerator.class})
class VampireLaceratorTest extends BaseCardTest {

    @Test
    @DisplayName("Controller loses 1 life during upkeep when no opponent has 10 or less life")
    void losesLifeWhenOpponentsAreAboveTenLife() {
        harness.addToBattlefield(player1, new VampireLacerator());
        harness.setLife(player1, 20);
        harness.setLife(player2, 11);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Controller does not lose life when an opponent has exactly 10 life")
    void doesNotLoseLifeAtTenOpponentLife() {
        harness.addToBattlefield(player1, new VampireLacerator());
        harness.setLife(player2, 10);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The unless condition is checked when the trigger resolves")
    void checksUnlessConditionAtResolution() {
        harness.addToBattlefield(player1, new VampireLacerator());
        harness.setLife(player2, 11);

        advanceToUpkeep(player1);
        harness.setLife(player2, 10);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The ability does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new VampireLacerator());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The ability still triggers at 10 opponent life and loses life if that total rises before resolution")
    void losesLifeWhenOpponentRisesAboveTenBeforeResolution() {
        harness.addToBattlefield(player1, new VampireLacerator());
        harness.setLife(player2, 10);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setLife(player2, 11);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 11);
    }

    @Test
    @DisplayName("An opponent below 10 life prevents the life loss even when the controller has more life")
    void doesNotLoseLifeBelowTenOpponentLife() {
        harness.addToBattlefield(player1, new VampireLacerator());
        harness.setLife(player2, 9);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 9);
    }

    @Test
    @DisplayName("The controller's own low life total does not prevent life loss")
    void lowControllerLifeDoesNotSatisfyUnlessCondition() {
        harness.addToBattlefield(player1, new VampireLacerator());
        harness.setLife(player1, 10);
        harness.setLife(player2, 11);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 9);
        harness.assertLife(player2, 11);
    }

    @Test
    @DisplayName("Each Vampire Lacerator causes its own life loss during its controller's upkeep")
    void multipleLaceratorsEachLoseOneLife() {
        harness.addToBattlefield(player1, new VampireLacerator());
        harness.addToBattlefield(player1, new VampireLacerator());
        harness.setLife(player2, 11);

        advanceToUpkeep(player1);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 11);
    }
}
