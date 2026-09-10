package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({AncientRunes.class, BottleGnomes.class})
class AncientRunesTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to the active player equal to the artifacts they control")
    void damagesActivePlayerByArtifactCount() {
        harness.addToBattlefield(player1, new AncientRunes());
        harness.addToBattlefield(player1, new BottleGnomes());
        harness.addToBattlefield(player1, new BottleGnomes());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Damages each player based on their own artifacts during their own upkeep")
    void damagesEachPlayerByOwnArtifacts() {
        harness.addToBattlefield(player1, new AncientRunes());
        harness.addToBattlefield(player2, new BottleGnomes());
        harness.addToBattlefield(player2, new BottleGnomes());
        harness.addToBattlefield(player2, new BottleGnomes());

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

    @Test
    @DisplayName("Counts artifacts when the upkeep trigger resolves")
    void countsArtifactsAtResolution() {
        harness.addToBattlefield(player1, new AncientRunes());
        harness.addToBattlefield(player1, new BottleGnomes());

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new BottleGnomes());
        harness.passBothPriorities(); // resolve trigger

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Does not count artifacts controlled by the nonactive player")
    void doesNotCountNonactivePlayersArtifacts() {
        harness.addToBattlefield(player1, new AncientRunes());
        harness.addToBattlefield(player1, new BottleGnomes());
        harness.addToBattlefield(player2, new BottleGnomes());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }
}
