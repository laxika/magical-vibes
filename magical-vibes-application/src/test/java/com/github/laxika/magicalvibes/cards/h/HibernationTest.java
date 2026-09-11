package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WildGrowth;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hibernation.class, Forest.class, GrizzlyBears.class, HillGiant.class, WildGrowth.class})
class HibernationTest extends BaseCardTest {

    @Test
    @DisplayName("Returns green permanents controlled by either player to their owners' hands")
    void returnsAllGreenPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castHibernation();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not return nongreen permanents")
    void doesNotReturnNongreenPermanents() {
        harness.addToBattlefield(player1, new HillGiant());
        castHibernation();

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertNotInHand(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Resolves with no green permanents in play")
    void resolvesWithNoGreenPermanents() {
        harness.addToBattlefield(player1, new HillGiant());
        castHibernation();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Returns a green Aura but leaves a colorless basic land on the battlefield")
    void returnsGreenAuraButNotColorlessBasicLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent wildGrowth = harness.addToBattlefieldAndReturn(player1, new WildGrowth());
        wildGrowth.setAttachedTo(forest.getId());

        castHibernation();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Wild Growth");
        harness.assertInHand(player1, "Wild Growth");
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Returns a stolen green permanent to its owner's hand")
    void returnsStolenGreenPermanentToItsOwner() {
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(stolen.getId(), player2.getId());

        castHibernation();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    private void castHibernation() {
        harness.castFromHand(player1, new Hibernation(), "{2}{U}");
        harness.passBothPriorities();
    }
}
