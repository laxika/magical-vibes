package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({FilterOut.class, FountainOfYouth.class, GrizzlyBears.class, Island.class,
        GloriousAnthem.class, Ornithopter.class, DarksteelCitadel.class})
class FilterOutTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all noncreature, nonland permanents to their owners' hands")
    void returnsAllNoncreatureNonlandPermanents() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Island());
        harness.castFromHand(player1, new FilterOut(), "{1}{U}{U}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player2, "Island");
        harness.assertInHand(player1, "Fountain of Youth");
        harness.assertInHand(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Returns enchantments but leaves artifact creatures and artifact lands")
    void respectsMultiplePermanentTypes() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new DarksteelCitadel());

        harness.castFromHand(player1, new FilterOut(), "{1}{U}{U}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInHand(player1, "Glorious Anthem");
        harness.assertInHand(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertOnBattlefield(player2, "Darksteel Citadel");
        harness.assertNotInHand(player1, "Ornithopter");
        harness.assertNotInHand(player2, "Darksteel Citadel");
    }

    @Test
    @DisplayName("Returns a permanent to its owner rather than its controller")
    void returnsToOwnerInsteadOfController() {
        FountainOfYouth fountain = new FountainOfYouth();
        fountain.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, fountain);

        harness.castFromHand(player1, new FilterOut(), "{1}{U}{U}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertInHand(player2, "Fountain of Youth");
        harness.assertNotInHand(player1, "Fountain of Youth");
    }

    @Test
    @DisplayName("Resolves normally when there are no matching permanents")
    void resolvesWithoutMatchingPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Island());

        harness.castFromHand(player1, new FilterOut(), "{1}{U}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Island");
        harness.assertInGraveyard(player1, "Filter Out");
    }
}
