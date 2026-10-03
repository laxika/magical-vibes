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

    @Test
    @DisplayName("A tapped Tablet still deals damage during the opponent's upkeep")
    void tappedTabletStillDealsDamage() {
        harness.addToBattlefieldAndReturn(player1, new CopperTablet()).setTapped(true);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Tablets controlled by both players each damage the active player")
    void multipleTabletsDamageOnlyActivePlayer() {
        harness.addToBattlefield(player1, new CopperTablet());
        harness.addToBattlefield(player2, new CopperTablet());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }
}
