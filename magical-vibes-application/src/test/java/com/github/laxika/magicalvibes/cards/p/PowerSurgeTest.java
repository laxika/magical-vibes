package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PowerSurgeTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP (snapshots untapped lands, pushes trigger)
    }

    @Test
    @DisplayName("Deals damage to the active player equal to their untapped lands")
    void damagesActivePlayerByUntappedLandCount() {
        harness.addToBattlefield(player1, new PowerSurge());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Damages each player based on their own lands during their own upkeep")
    void damagesEachPlayerByOwnLands() {
        harness.addToBattlefield(player1, new PowerSurge());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Tapping lands in response to the trigger does not reduce the damage")
    void tappingInResponseDoesNotReduceDamage() {
        harness.addToBattlefield(player1, new PowerSurge());
        harness.addToBattlefield(player1, new Swamp()); // index 1
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        advanceToUpkeep(player1); // snapshot = 3 untapped lands
        harness.tapPermanent(player1, 1); // tap a land after the trigger is on the stack
        harness.passBothPriorities(); // resolve trigger — still 3, not 2

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Deals no damage when the active player controls no lands")
    void noDamageWithoutLands() {
        harness.addToBattlefield(player1, new PowerSurge());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.assertLife(player1, 20);
    }
}
