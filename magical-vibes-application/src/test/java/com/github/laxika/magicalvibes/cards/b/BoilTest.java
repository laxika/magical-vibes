package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({Boil.class, GrizzlyBears.class, Island.class, Mountain.class, Plains.class})
class BoilTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all Islands controlled by both players")
    void destroysAllIslands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        castBoilAndResolve();

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
        castBoilAndResolve();

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Indestructible Islands survive Boil")
    void indestructibleIslandSurvives() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        island.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        castBoilAndResolve();

        harness.assertOnBattlefield(player2, "Island");
        harness.assertNotInGraveyard(player2, "Island");
    }

    private void castBoilAndResolve() {
        harness.castFromHand(player1, new Boil(), "{3}{R}");
        harness.passBothPriorities();
    }
}
