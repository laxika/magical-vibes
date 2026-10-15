package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({PowerSurge.class, Swamp.class, Disenchant.class})
class PowerSurgeTest extends BaseCardTest {

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

    @Test
    void doesNotCountLandTappedAtBeginningOfTurn() {
        harness.addToBattlefield(player1, new PowerSurge());
        harness.addToBattlefield(player1, new Swamp()); // index 1
        harness.addToBattlefield(player1, new Swamp());
        harness.tapPermanent(player1, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    void doesNotCountOtherPlayersLands() {
        harness.addToBattlefield(player1, new PowerSurge());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotCountLandEnteringAfterTurnBegins() {
        harness.addToBattlefield(player1, new PowerSurge());
        harness.addToBattlefield(player1, new Swamp());

        advanceToUpkeep(player1);
        harness.enterBattlefieldAndReturn(player1, new Swamp());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    void eachCopyDealsDamageIndependently() {
        harness.addToBattlefield(player1, new PowerSurge());
        harness.addToBattlefield(player2, new PowerSurge());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    void destroyingSourceInResponseDoesNotStopDamage() {
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new PowerSurge()).getId();
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        advanceToUpkeep(player1);
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, sourceId);
        harness.assertNotOnBattlefield(player1, "Power Surge");
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }
}
