package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BeastOfBurden;
import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.g.GrimMonolith;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({Rebuild.class, BeastOfBurden.class, GiantCockroach.class, GrimMonolith.class})
class RebuildTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all artifacts, including artifact creatures, to their owners' hands")
    void returnsAllArtifactsToOwnersHands() {
        harness.addToBattlefield(player1, new BeastOfBurden());
        harness.addToBattlefield(player2, new GrimMonolith());
        harness.addToBattlefield(player2, new GiantCockroach());

        harness.castFromHand(player1, new Rebuild(), "{2}{U}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Beast of Burden");
        harness.assertNotOnBattlefield(player2, "Grim Monolith");
        harness.assertOnBattlefield(player2, "Giant Cockroach");
        harness.assertInHand(player1, "Beast of Burden");
        harness.assertInHand(player2, "Grim Monolith");
    }

    @Test
    @DisplayName("Returns a stolen artifact to its owner's hand")
    void returnsStolenArtifactToItsOwner() {
        GrimMonolith stolenArtifactCard = new GrimMonolith();
        stolenArtifactCard.setOwnerId(player1.getId());
        var stolenArtifact = harness.addToBattlefieldAndReturn(player2, stolenArtifactCard);
        gd.stolenCreatures.put(stolenArtifact.getId(), player1.getId());
        harness.setHand(player2, List.of());

        harness.castFromHand(player1, new Rebuild(), "{2}{U}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grim Monolith");
        harness.assertInHand(player1, "Grim Monolith");
        harness.assertNotInHand(player2, "Grim Monolith");
    }

    @Test
    @DisplayName("Cycling discards Rebuild and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Rebuild()));
        harness.setLibrary(player1, List.of(new GiantCockroach()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rebuild");
        harness.assertInHand(player1, "Giant Cockroach");
    }
}
