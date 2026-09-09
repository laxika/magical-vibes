package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed(CopperTablet.class)
class CopperTabletTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to the active player during each player's upkeep")
    void damagesActivePlayerDuringEachUpkeep() {
        harness.addToBattlefield(player1, new CopperTablet());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Damages the opponent during their upkeep")
    void damagesOpponentDuringTheirUpkeep() {
        harness.addToBattlefield(player1, new CopperTablet());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }
}
