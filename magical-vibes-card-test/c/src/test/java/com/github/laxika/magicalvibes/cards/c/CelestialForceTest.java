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

    @Test
    @DisplayName("Life is gained when the upkeep trigger resolves, not when it triggers")
    void gainsLifeOnlyOnResolution() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new CelestialForce());

        advanceToUpkeep(player2);

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);

        resolveAllTriggers();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each copy grants its controller 3 life during the same upkeep")
    void multipleCopiesTriggerIndependently() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new CelestialForce());
        harness.addToBattlefield(player1, new CelestialForce());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opposing copies each grant life to their own controller")
    void opposingCopiesGainLifeForTheirControllers() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 12);
        harness.addToBattlefield(player1, new CelestialForce());
        harness.addToBattlefield(player2, new CelestialForce());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 15);
    }
}
