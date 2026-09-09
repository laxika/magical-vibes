package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.ArdentMilitia;
import com.github.laxika.magicalvibes.cards.l.LlanowarDruid;
import com.github.laxika.magicalvibes.cards.n.NullRod;
import com.github.laxika.magicalvibes.cards.w.WindingCanyons;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({UrborgStalker.class, ArdentMilitia.class, NullRod.class, WindingCanyons.class,
        LlanowarDruid.class})
class UrborgStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to the active player who controls a nonblack, nonland permanent")
    void damagesActivePlayerWithNonblackPermanent() {
        harness.addToBattlefield(player1, new UrborgStalker());
        harness.addToBattlefield(player1, new ArdentMilitia());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Colorless permanents count as nonblack")
    void colorlessPermanentCounts() {
        harness.addToBattlefield(player1, new UrborgStalker());
        harness.addToBattlefield(player1, new NullRod());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Deals no damage when the active player controls only black permanents and lands")
    void noDamageWithOnlyBlackPermanentsAndLands() {
        harness.addToBattlefield(player1, new UrborgStalker());
        harness.addToBattlefield(player1, new WindingCanyons());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Damages the opponent on their own upkeep and leaves the controller alone")
    void damagesOpponentOnTheirUpkeep() {
        harness.addToBattlefield(player1, new UrborgStalker());
        harness.addToBattlefield(player1, new ArdentMilitia());
        harness.addToBattlefield(player2, new ArdentMilitia());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not deal damage if the condition is false when the trigger resolves")
    void doesNotDamageWhenMatchingPermanentLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new UrborgStalker());
        addCreatureReady(player1, new LlanowarDruid());

        advanceToUpkeep(player1);
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Llanowar Druid");
        harness.assertLife(player1, 20);
    }
}
