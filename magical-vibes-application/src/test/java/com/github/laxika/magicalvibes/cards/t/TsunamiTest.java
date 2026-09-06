package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({Forest.class, Island.class, Tsunami.class, Tundra.class})
class TsunamiTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all Islands controlled by both players")
    void destroysAllIslands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.castFromHand(player1, new Tsunami(), "{3}{G}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInGraveyard(player1, "Island");
        harness.assertInGraveyard(player2, "Island");
    }

    @Test
    @DisplayName("Does not destroy non-Island lands")
    void doesNotDestroyNonIslands() {
        harness.addToBattlefield(player1, new Forest());

        harness.castFromHand(player1, new Tsunami(), "{3}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Destroys nonbasic lands with the Island subtype")
    void destroysNonbasicIslands() {
        harness.addToBattlefield(player1, new Tundra());
        harness.addToBattlefield(player2, new Forest());

        harness.castFromHand(player1, new Tsunami(), "{3}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tundra");
        harness.assertInGraveyard(player1, "Tundra");
        harness.assertOnBattlefield(player2, "Forest");
    }
}
