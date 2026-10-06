package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({ScourgeOfFleets.class, Island.class, GrizzlyBears.class, HillGiant.class})
class ScourgeOfFleetsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns opposing creatures with toughness at most the number of Islands you control")
    void returnsOpposingCreaturesWithinIslandThreshold() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.castFromHand(player1, new ScourgeOfFleets(), "{5}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Determines the Island count when the ETB ability resolves")
    void determinesIslandCountAtResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new ScourgeOfFleets(), "{5}{U}{U}");
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not return creatures you control")
    void doesNotReturnYourCreatures() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new ScourgeOfFleets(), "{5}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opposing Islands do not increase the threshold")
    void doesNotCountOpposingIslands() {
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castFromHand(player1, new ScourgeOfFleets(), "{5}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("Returns every qualifying creature and leaves noncreature permanents")
    void returnsAllQualifyingCreatures() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new Island());

        harness.castFromHand(player1, new ScourgeOfFleets(), "{5}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("Uses current toughness when the trigger resolves")
    void checksModifiedToughnessAtResolution() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new ScourgeOfFleets(), "{5}{U}{U}");
        harness.passBothPriorities();
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The trigger still resolves after Scourge of Fleets leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castFromHand(player1, new ScourgeOfFleets(), "{5}{U}{U}");
        harness.passBothPriorities();
        var scourge = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof ScourgeOfFleets)
                .findFirst().orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, scourge));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Scourge of Fleets");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }
}
