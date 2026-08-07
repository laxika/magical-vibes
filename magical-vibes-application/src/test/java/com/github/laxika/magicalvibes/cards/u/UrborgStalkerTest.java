package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UrborgStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to the active player who controls a nonblack, nonland permanent")
    void damagesActivePlayerWithNonblackPermanent() {
        harness.addToBattlefield(player1, new UrborgStalker());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Colorless permanents count as nonblack")
    void colorlessPermanentCounts() {
        harness.addToBattlefield(player1, new UrborgStalker());
        harness.addToBattlefield(player1, new Ornithopter());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Deals no damage when the active player controls only black permanents and lands")
    void noDamageWithOnlyBlackPermanentsAndLands() {
        harness.addToBattlefield(player1, new UrborgStalker());
        harness.addToBattlefield(player1, new Swamp());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Damages the opponent on their own upkeep and leaves the controller alone")
    void damagesOpponentOnTheirUpkeep() {
        harness.addToBattlefield(player1, new UrborgStalker());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }
}
