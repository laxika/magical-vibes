package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({EngulfTheShore.class, GrizzlyBears.class, HillGiant.class, Island.class})
class EngulfTheShoreTest extends BaseCardTest {

    @Test
    @DisplayName("Returns creatures with toughness at most the number of Islands you control")
    void returnsCreaturesWithinIslandThreshold() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        castEngulfTheShore();

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Determines the Island count when the spell resolves")
    void determinesIslandCountAtResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castEngulfTheShore();

        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not return creatures when you control no Islands")
    void doesNotReturnCreaturesWithoutIslands() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castEngulfTheShore();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opposing Islands do not increase the threshold and Islands remain on the battlefield")
    void countsOnlyCastersIslands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castEngulfTheShore();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("Uses modified toughness rather than printed toughness")
    void usesEffectiveToughness() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castEngulfTheShore();
        bears.setToughnessModifier(1);
        giant.setToughnessModifier(-1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Marked damage does not reduce toughness for the threshold")
    void ignoresMarkedDamage() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        giant.setMarkedDamage(2);
        castEngulfTheShore();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertNotInHand(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Returns a stolen creature to its owner rather than its controller")
    void returnsStolenCreatureToOwner() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(bears.getId(), player2.getId());
        castEngulfTheShore();

        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    private void castEngulfTheShore() {
        harness.castFromHand(player1, new EngulfTheShore(), "{3}{U}");
    }
}
