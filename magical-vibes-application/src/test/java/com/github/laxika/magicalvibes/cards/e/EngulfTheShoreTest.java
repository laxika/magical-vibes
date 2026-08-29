package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    private void castEngulfTheShore() {
        harness.setHand(player1, List.of(new EngulfTheShore()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0);
    }
}
