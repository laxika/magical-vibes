package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.TheEternalWanderer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrabrasksAnointer.class, Forest.class, Swamp.class, TheEternalWanderer.class})
class UrabrasksAnointerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of permanents you control with oil counters")
    void dealsDamageForOilCounterPermanents() {
        Permanent firstPermanent = harness.addToBattlefieldAndReturn(player1, new Forest());
        firstPermanent.setCounterCount(CounterType.OIL, 1);
        Permanent secondPermanent = harness.addToBattlefieldAndReturn(player1, new Swamp());
        secondPermanent.setCounterCount(CounterType.OIL, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UrabrasksAnointer());

        castUrabrasksAnointer(player1);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts permanents rather than the number of oil counters")
    void countsPermanentsNotOilCounters() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new Forest());
        ownPermanent.setCounterCount(CounterType.OIL, 3);
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new Swamp());
        opponentPermanent.setCounterCount(CounterType.OIL, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UrabrasksAnointer());

        castUrabrasksAnointer(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void dealsZeroDamageWithoutOilCounters() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new Forest());
        ownPermanent.setCounterCount(CounterType.CHARGE, 3);
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new Swamp());
        opponentPermanent.setCounterCount(CounterType.OIL, 4);

        castUrabrasksAnointer(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countsOilCountersAddedBeforeResolutionIncludingOnItself() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        castUrabrasksAnointer(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        land.setCounterCount(CounterType.OIL, 3);
        findPermanent(player1, "Urabrask's Anointer").setCounterCount(CounterType.OIL, 2);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void stopsCountingPermanentsWhoseLastOilCounterWasRemoved() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.setCounterCount(CounterType.OIL, 1);

        castUrabrasksAnointer(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        land.setCounterCount(CounterType.OIL, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    void canDamageItsController() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.setCounterCount(CounterType.OIL, 1);

        castUrabrasksAnointer(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    void canTargetItselfAndDealLethalDamage() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        firstLand.setCounterCount(CounterType.OIL, 1);
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Swamp());
        secondLand.setCounterCount(CounterType.OIL, 1);

        castUrabrasksAnointer(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, findPermanent(player1, "Urabrask's Anointer").getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Urabrask's Anointer");
        harness.assertInGraveyard(player1, "Urabrask's Anointer");
    }

    @Test
    void canDamagePlaneswalkers() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.setCounterCount(CounterType.OIL, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TheEternalWanderer());
        target.setCounterCount(CounterType.LOYALTY, 5);

        castUrabrasksAnointer(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertOnBattlefield(player2, "The Eternal Wanderer");
    }

    private void castUrabrasksAnointer(com.github.laxika.magicalvibes.model.Player player) {
        harness.castFromHand(player, new UrabrasksAnointer(), "{3}{R}");
    }
}
