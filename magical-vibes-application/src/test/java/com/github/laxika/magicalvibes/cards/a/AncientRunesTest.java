package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AncientRunesTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to the active player equal to the artifacts they control")
    void damagesActivePlayerByArtifactCount() {
        harness.addToBattlefield(player1, new AncientRunes());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Damages each player based on their own artifacts during their own upkeep")
    void damagesEachPlayerByOwnArtifacts() {
        harness.addToBattlefield(player1, new AncientRunes());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Deals no damage when the active player controls no artifacts")
    void noDamageWithoutArtifacts() {
        harness.addToBattlefield(player1, new AncientRunes());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.assertLife(player1, 20);
    }
}
