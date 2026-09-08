package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class RebuildTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all artifacts to their owners' hands")
    void returnsAllArtifactsToOwnersHands() {
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Rebuild()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Angel's Feather");
        harness.assertNotOnBattlefield(player2, "Icy Manipulator");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Angel's Feather");
        harness.assertInHand(player2, "Icy Manipulator");
    }

    @Test
    @DisplayName("Cycling discards Rebuild and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Rebuild()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rebuild");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
