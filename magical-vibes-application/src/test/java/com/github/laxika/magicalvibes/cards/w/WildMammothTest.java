package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.StampedeDriver;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({WildMammoth.class, StampedeDriver.class})
class WildMammothTest extends BaseCardTest {

    @Test
    @DisplayName("The player with the most creatures gains control during upkeep")
    void playerWithMostCreaturesGainsControl() {
        harness.addToBattlefield(player1, new WildMammoth());
        harness.addToBattlefield(player2, new StampedeDriver());
        harness.addToBattlefield(player2, new StampedeDriver());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wild Mammoth");
        harness.assertOnBattlefield(player2, "Wild Mammoth");
    }

    @Test
    @DisplayName("The controller keeps Wild Mammoth when they control the most creatures")
    void controllerKeepsWhenTheyHaveMostCreatures() {
        harness.addToBattlefield(player1, new WildMammoth());
        harness.addToBattlefield(player1, new StampedeDriver());
        harness.addToBattlefield(player2, new StampedeDriver());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wild Mammoth");
        harness.assertNotOnBattlefield(player2, "Wild Mammoth");
    }

    @Test
    @DisplayName("Wild Mammoth does not change control when creature counts are tied")
    void noChangeOnTie() {
        harness.addToBattlefield(player1, new WildMammoth());
        harness.addToBattlefield(player2, new StampedeDriver());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wild Mammoth");
        harness.assertNotOnBattlefield(player2, "Wild Mammoth");
    }

    @Test
    @DisplayName("The creature-count condition is checked again when the ability resolves")
    void doesNothingIfCreatureCountsAreNoLongerUniqueAtResolution() {
        harness.addToBattlefield(player1, new WildMammoth());
        harness.addToBattlefield(player2, new StampedeDriver());
        harness.addToBattlefield(player2, new StampedeDriver());

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new StampedeDriver());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wild Mammoth");
        harness.assertNotOnBattlefield(player2, "Wild Mammoth");
    }
}
