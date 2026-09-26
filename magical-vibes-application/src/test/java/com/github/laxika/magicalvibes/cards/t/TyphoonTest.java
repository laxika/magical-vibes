package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({Typhoon.class, Island.class, Tolaria.class})
class TyphoonTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to each opponent equal to that opponent's Island count")
    void dealsDamageBasedOnEachOpponentsIslandCount() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Tolaria());
        castTyphoon();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Deals no damage when an opponent controls no Islands")
    void dealsNoDamageWithoutIslands() {
        harness.addToBattlefield(player2, new Tolaria());
        castTyphoon();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Counts Islands when the spell resolves")
    void countsIslandsAtResolution() {
        harness.castFromHand(player1, new Typhoon(), "{2}{G}");

        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    private void castTyphoon() {
        harness.castFromHand(player1, new Typhoon(), "{2}{G}");
        harness.passBothPriorities();
    }
}
