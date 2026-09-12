package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({Rescind.class, Island.class})
class RescindTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target permanent to its owner's hand")
    void returnsTargetPermanentToHand() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new Rescind()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Island"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInHand(player2, "Island");
    }

    @Test
    @DisplayName("Returns a stolen permanent to its owner's hand, not its controller's")
    void returnsStolenPermanentToItsOwnerHand() {
        Permanent stolenIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        gd.stolenCreatures.put(stolenIsland.getId(), player1.getId());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Rescind()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, stolenIsland.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInHand(player1, "Island");
        harness.assertNotInHand(player2, "Island");
    }

    @Test
    @DisplayName("Cycling {2} discards Rescind and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Rescind()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rescind");
        harness.assertInHand(player1, "Island");
    }
}
