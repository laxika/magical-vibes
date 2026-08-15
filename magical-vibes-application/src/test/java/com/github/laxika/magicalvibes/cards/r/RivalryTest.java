package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RivalryTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to the active player who controls more lands than each other player")
    void damagesActivePlayerWithMostLands() {
        harness.addToBattlefield(player1, new Rivalry());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not trigger when the active player is tied for most lands")
    void doesNotTriggerOnLandCountTie() {
        harness.addToBattlefield(player1, new Rivalry());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not deal damage if the active player loses the land lead before resolution")
    void doesNotDealDamageAfterLandLeadIsLost() {
        harness.addToBattlefield(player1, new Rivalry());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Forest"));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }
}
