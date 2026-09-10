package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({NaturesRuin.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class NaturesRuinTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys green creatures controlled by both players")
    void destroysGreenCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new NaturesRuin(), "{2}{B}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Leaves non-green creatures untouched")
    void leavesNonGreenCreatures() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.castFromHand(player1, new NaturesRuin(), "{2}{B}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Leaves noncreature permanents untouched")
    void leavesNoncreaturePermanents() {
        harness.addToBattlefield(player1, new Forest());
        harness.castFromHand(player1, new NaturesRuin(), "{2}{B}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Destroys only green creatures among a mixed board")
    void destroysOnlyGreenAmongMixed() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.castFromHand(player1, new NaturesRuin(), "{2}{B}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
    }
}
