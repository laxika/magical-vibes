package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.TerramorphicExpanse;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({FurnacePunisher.class, Mountain.class, TerramorphicExpanse.class})
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
        harness.addToBattlefield(player1, new TerramorphicExpanse());

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

    @Test
    @DisplayName("Still triggers with two basic lands and deals damage if one leaves before resolution")
    void damagesPlayerWhoLosesSecondBasicLandBeforeResolution() {
        harness.addToBattlefield(player1, new FurnacePunisher());
        harness.addToBattlefield(player1, new Mountain());
        var secondLand = harness.addToBattlefieldAndReturn(player1, new Mountain());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(secondLand);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The controller's basic lands do not protect an opponent with no basic lands")
    void doesNotCountControllersBasicLandsDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new FurnacePunisher());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An opponent with more than two basic lands takes no damage")
    void doesNotDamageOpponentWithMoreThanTwoBasicLands() {
        harness.addToBattlefield(player1, new FurnacePunisher());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }
}
