package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class BoilTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all Islands controlled by both players")
    void destroysAllIslands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new Boil()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInGraveyard(player1, "Island");
        harness.assertInGraveyard(player2, "Island");
    }

    @Test
    @DisplayName("Does not destroy other lands or creatures")
    void doesNotDestroyNonIslands() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Boil()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
