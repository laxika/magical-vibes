package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScourgeOfFleetsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns opposing creatures with toughness at most the number of Islands you control")
    void returnsOpposingCreaturesWithinIslandThreshold() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new ScourgeOfFleets()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Determines the Island count when the ETB ability resolves")
    void determinesIslandCountAtResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScourgeOfFleets()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
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
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScourgeOfFleets()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
