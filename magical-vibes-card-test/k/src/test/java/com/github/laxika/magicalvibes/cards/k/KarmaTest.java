package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({Karma.class, Swamp.class, Demystify.class})
class KarmaTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to the active player equal to the Swamps they control")
    void damagesActivePlayerBySwampCount() {
        harness.addToBattlefield(player1, new Karma());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Damages each player based on their own Swamps during their own upkeep")
    void damagesEachPlayerByOwnSwamps() {
        harness.addToBattlefield(player1, new Karma());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        // Only player2 is damaged (2 Swamps); player1 controls no Swamps and isn't the active player
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not count Swamps controlled by Karma's controller")
    void doesNotCountControllerSwampsForOpponentsUpkeep() {
        harness.addToBattlefield(player1, new Karma());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Deals no damage when the active player controls no Swamps")
    void noDamageWithoutSwamps() {
        harness.addToBattlefield(player1, new Karma());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each Karma triggers independently")
    void multipleKarmasTriggerIndependently() {
        harness.addToBattlefield(player1, new Karma());
        harness.addToBattlefield(player1, new Karma());
        harness.addToBattlefield(player1, new Swamp());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Counts Swamps when the upkeep trigger resolves")
    void countsSwampsAtResolution() {
        harness.addToBattlefield(player1, new Karma());
        harness.addToBattlefield(player1, new Swamp());

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new Swamp());
        harness.passBothPriorities(); // resolve trigger

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Counts tapped Swamps as well as untapped Swamps")
    void countsTappedSwamps() {
        harness.addToBattlefield(player1, new Karma());
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());

        advanceToUpkeep(player1);
        swamp.tap();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An upkeep trigger still deals damage after Karma is destroyed")
    void triggerResolvesAfterKarmaLeavesBattlefield() {
        Permanent karma = harness.addToBattlefieldAndReturn(player1, new Karma());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        advanceToUpkeep(player2);
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0, karma.getId());
        harness.assertInGraveyard(player1, "Karma");
        harness.assertNotOnBattlefield(player1, "Karma");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }
}
