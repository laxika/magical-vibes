package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Convalescence.class)
class ConvalescenceTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life at upkeep when its controller has 10 or less life")
    void gainsLifeAtOrBelowThreshold() {
        harness.addToBattlefield(player1, new Convalescence());
        harness.setLife(player1, 10);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Does not gain life at upkeep when its controller has more than 10 life")
    void doesNotGainLifeAboveThreshold() {
        harness.addToBattlefield(player1, new Convalescence());
        harness.setLife(player1, 11);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Does not gain life if the condition stops being true before resolution")
    void doesNotGainLifeWhenLifeRisesBeforeResolution() {
        harness.addToBattlefield(player1, new Convalescence());
        harness.setLife(player1, 10);

        advanceToUpkeep(player1);
        harness.setLife(player1, 11);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Triggers only during its controller's upkeep")
    void triggersOnlyDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new Convalescence());
        harness.setLife(player1, 10);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }
}
