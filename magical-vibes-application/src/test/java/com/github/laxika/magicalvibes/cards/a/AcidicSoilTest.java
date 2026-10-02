package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CaveTiger;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({AcidicSoil.class, Forest.class, Mountain.class, CaveTiger.class})
class AcidicSoilTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to each player equal to their land count")
    void dealsDamageBasedOnEachPlayersLandCount() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        harness.castFromHand(player1, new AcidicSoil(), "{2}{R}");
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals no damage to a player who controls no lands")
    void noLandsMeansNoDamage() {
        harness.addToBattlefield(player1, new Forest());

        harness.castFromHand(player1, new AcidicSoil(), "{2}{R}");
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Counts lands at resolution")
    void countsLandsAtResolution() {
        harness.addToBattlefield(player1, new Forest());

        harness.castFromHand(player1, new AcidicSoil(), "{2}{R}");

        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Counts tapped lands but excludes nonlands and lands outside the battlefield")
    void countsOnlyLandsOnBattlefieldRegardlessOfTappedStatus() {
        harness.addToBattlefieldAndReturn(player1, new Forest()).tap();
        harness.addToBattlefield(player1, new CaveTiger());
        harness.addToBattlefieldAndReturn(player2, new Mountain()).tap();
        harness.addToBattlefield(player2, new CaveTiger());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new Mountain()));
        harness.setHand(player2, List.of(new Forest()));
        harness.setExile(player1, List.of(new Mountain()));

        harness.castFromHand(player1, new AcidicSoil(), "{2}{R}");
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Cave Tiger");
        harness.assertOnBattlefield(player2, "Cave Tiger");
        harness.assertInGraveyard(player1, "Acidic Soil");
    }
}
