package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.TropicalIsland;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

@CardUsed({AcidRain.class, Forest.class, Island.class, GrizzlyBears.class})
class AcidRainTest extends BaseCardTest {

    @Test
    void destroysAllForestsAndLeavesOtherPermanents() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new AcidRain(), "{3}{U}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @CardUsed(TropicalIsland.class)
    void destroysNonbasicForestsWithMultipleLandTypes() {
        harness.addToBattlefield(player1, new TropicalIsland());
        harness.addToBattlefield(player2, new TropicalIsland());
        harness.addToBattlefield(player2, new Island());

        harness.castFromHand(player1, new AcidRain(), "{3}{U}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tropical Island");
        harness.assertNotOnBattlefield(player2, "Tropical Island");
        harness.assertInGraveyard(player1, "Tropical Island");
        harness.assertInGraveyard(player2, "Tropical Island");
        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    void resolvesWithoutAnyForests() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castFromHand(player1, new AcidRain(), "{3}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Acid Rain");
    }
}
