package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DwarvenRuins;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FurnacePunisherTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to the active player with fewer than two basic lands")
    void damagesActivePlayerWithFewerThanTwoBasicLands() {
        harness.addToBattlefield(player1, new FurnacePunisher());
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Deals no damage to the active player with two basic lands")
    void doesNotDamageActivePlayerWithTwoBasicLands() {
        harness.addToBattlefield(player1, new FurnacePunisher());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Counts only basic lands")
    void countsOnlyBasicLands() {
        harness.addToBattlefield(player1, new FurnacePunisher());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new DwarvenRuins());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Checks the basic-land condition as the trigger resolves")
    void checksConditionAtResolution() {
        harness.addToBattlefield(player1, new FurnacePunisher());
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new Mountain());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Deals damage to the opponent during their upkeep")
    void damagesOpponentDuringTheirUpkeep() {
        harness.addToBattlefield(player1, new FurnacePunisher());
        harness.addToBattlefield(player2, new Mountain());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }
}
