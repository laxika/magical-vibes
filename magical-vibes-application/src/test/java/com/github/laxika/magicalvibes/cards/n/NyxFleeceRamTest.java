package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({NyxFleeceRam.class})
class NyxFleeceRamTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life during its controller's upkeep")
    void gainsLifeDuringControllerUpkeep() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new NyxFleeceRam());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new NyxFleeceRam());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Life is gained only when the upkeep trigger resolves")
    void lifeGainWaitsForResolution() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new NyxFleeceRam());

        advanceToUpkeep(player1);

        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 11);
    }

    @Test
    @DisplayName("Each Ram gains life independently during its controller's upkeep")
    void multipleRamsEachGainLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.addToBattlefield(player1, new NyxFleeceRam());
        harness.addToBattlefield(player1, new NyxFleeceRam());
        harness.addToBattlefield(player2, new NyxFleeceRam());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("A Ram controlled by the second player gains life for that player")
    void secondPlayerGainsLifeOnTheirUpkeep() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.addToBattlefield(player2, new NyxFleeceRam());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 11);
    }
}
