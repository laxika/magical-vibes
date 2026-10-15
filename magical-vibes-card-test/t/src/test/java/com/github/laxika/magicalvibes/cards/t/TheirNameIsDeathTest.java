package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.c.CanoptekScarabSwarm;
import com.github.laxika.magicalvibes.cards.p.PlagueDrone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({TheirNameIsDeath.class, GrizzlyBears.class, IronMyr.class, HowlingMine.class,
        CanoptekScarabSwarm.class, PlagueDrone.class})
class TheirNameIsDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys nonartifact creatures and leaves artifact creatures and other permanents untouched")
    void destroysNonartifactCreaturesOnly() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new IronMyr());
        harness.addToBattlefield(player1, new HowlingMine());

        harness.castFromHand(player1, new TheirNameIsDeath(), "{3}{B}{B}{B}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Iron Myr");
        harness.assertOnBattlefield(player1, "Howling Mine");
        harness.assertInGraveyard(player1, "Their Name Is Death");
    }

    @Test
    @DisplayName("Resolves without targets when no creatures are present")
    void resolvesOnEmptyBattlefield() {
        harness.castFromHand(player1, new TheirNameIsDeath(), "{3}{B}{B}{B}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Their Name Is Death");
    }

    @Test
    @DisplayName("Checks which creatures to destroy at resolution")
    void destroysCreaturesThatArriveAfterCasting() {
        harness.castFromHand(player1, new TheirNameIsDeath(), "{3}{B}{B}{B}");
        harness.addToBattlefield(player2, new PlagueDrone());
        harness.addToBattlefield(player2, new CanoptekScarabSwarm());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Plague Drone");
        harness.assertInGraveyard(player2, "Plague Drone");
        harness.assertOnBattlefield(player2, "Canoptek Scarab Swarm");
        harness.assertNotInGraveyard(player2, "Canoptek Scarab Swarm");
        harness.assertInGraveyard(player1, "Their Name Is Death");
    }
}
