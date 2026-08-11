package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
