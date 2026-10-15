package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KarplusanForest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({PrimalOrder.class, KarplusanForest.class, Forest.class, Boomerang.class, Disenchant.class})
class PrimalOrderTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to active player equal to their nonbasic lands")
    void damagesActivePlayerByNonbasicLandCount() {
        harness.addToBattlefield(player1, new PrimalOrder());
        harness.addToBattlefield(player1, new KarplusanForest());
        harness.addToBattlefield(player1, new KarplusanForest());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Basic lands are not counted")
    void basicLandsDoNotCount() {
        harness.addToBattlefield(player1, new PrimalOrder());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("On opponent's upkeep, damages the opponent by their own nonbasic lands")
    void damagesOpponentByTheirNonbasicLands() {
        harness.addToBattlefield(player1, new PrimalOrder());
        harness.addToBattlefield(player2, new KarplusanForest());
        harness.addToBattlefield(player1, new KarplusanForest());
        harness.addToBattlefield(player1, new KarplusanForest());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        // Only player2's single nonbasic land counts; controller is untouched.
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Counts nonbasic lands when the trigger resolves")
    void countsNonbasicLandsAtResolution() {
        harness.addToBattlefield(player1, new PrimalOrder());
        harness.addToBattlefield(player1, new KarplusanForest());

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new KarplusanForest());
        harness.passBothPriorities(); // resolve trigger

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Counts only nonbasic lands among mixed permanents")
    void countsOnlyNonbasicLandsAmongMixedPermanents() {
        harness.addToBattlefield(player1, new PrimalOrder());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new KarplusanForest());
        harness.addToBattlefield(player2, new KarplusanForest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A nonbasic land returned before resolution no longer counts")
    void excludesLandReturnedBeforeResolution() {
        harness.addToBattlefield(player1, new PrimalOrder());
        harness.addToBattlefield(player1, new KarplusanForest());
        harness.setHand(player1, List.of(new Boomerang()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Karplusan Forest"));
        harness.assertInHand(player1, "Karplusan Forest");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Destroying Primal Order does not stop its pending upkeep damage")
    void triggerResolvesAfterSourceDestroyed() {
        harness.addToBattlefield(player1, new PrimalOrder());
        harness.addToBattlefield(player1, new KarplusanForest());
        harness.setHand(player1, List.of(new Disenchant()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Primal Order"));
        harness.assertInGraveyard(player1, "Primal Order");
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Primal Orders controlled by different players each deal upkeep damage")
    void multipleOrdersEachDamageActivePlayer() {
        harness.addToBattlefield(player1, new PrimalOrder());
        harness.addToBattlefield(player2, new PrimalOrder());
        harness.addToBattlefield(player2, new KarplusanForest());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }
}
