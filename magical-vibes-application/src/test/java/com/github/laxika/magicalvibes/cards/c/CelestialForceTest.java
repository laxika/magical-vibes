package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CelestialForce.class)
class CelestialForceTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 3 life during its controller's upkeep")
    void gainsLifeDuringControllerUpkeep() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new CelestialForce());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Gains 3 life during an opponent's upkeep")
    void gainsLifeDuringOpponentsUpkeep() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new CelestialForce());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
