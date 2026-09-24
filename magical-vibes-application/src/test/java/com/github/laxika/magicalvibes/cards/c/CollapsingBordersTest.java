package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({CollapsingBorders.class, Forest.class, Island.class, Mountain.class, Plains.class, Swamp.class})
class CollapsingBordersTest extends BaseCardTest {

    @Test
    @DisplayName("Active player gains life for their basic land types, then takes 3 damage")
    void usesActivePlayersDomainCountAndDealsDamageToThem() {
        harness.addToBattlefield(player1, new CollapsingBorders());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Swamp());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opponent's upkeep uses the opponent's basic land types")
    void usesOpponentsDomainCountOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new CollapsingBorders());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Forest());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Counts each basic land type only once")
    void countsDistinctBasicLandTypes() {
        harness.addToBattlefield(player1, new CollapsingBorders());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deals 3 damage when the active player controls no basic land types")
    void dealsDamageWithNoBasicLandTypes() {
        harness.addToBattlefield(player1, new CollapsingBorders());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }
}
