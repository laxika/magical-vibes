package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFalconSamWilson.class, GrizzlyBears.class, Plains.class})
class TheFalconSamWilsonTest extends BaseCardTest {

    @Test
    @DisplayName("Its enter-the-battlefield ability puts a +1/+1 counter on each other creature you control")
    void putsCountersOnOtherCreaturesYouControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new TheFalconSamWilson(), "{4}{W}");
        resolveAllTriggers();

        Permanent falcon = findPermanent(player1, "The Falcon, Sam Wilson");
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(falcon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Every other creature gets one additional counter, but noncreatures get none")
    void countersEachCreatureAndIgnoresNoncreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.castFromHand(player1, new TheFalconSamWilson(), "{4}{W}");
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "The Falcon, Sam Wilson")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The trigger includes creatures that enter before it resolves")
    void determinesEligibleCreaturesAtResolution() {
        harness.castFromHand(player1, new TheFalconSamWilson(), "{4}{W}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        resolveAllTriggers();

        assertThat(lateCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "The Falcon, Sam Wilson")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The enter trigger still puts counters on other creatures after The Falcon leaves")
    void triggerResolvesAfterSourceLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new TheFalconSamWilson(), "{4}{W}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        Permanent falcon = findPermanent(player1, "The Falcon, Sam Wilson");
        gd.playerBattlefields.get(player1.getId()).remove(falcon);
        gd.playerGraveyards.get(player1.getId()).add(falcon.getCard());

        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering alone resolves without putting a counter on itself")
    void enteringWithNoOtherCreaturesDoesNothing() {
        harness.castFromHand(player1, new TheFalconSamWilson(), "{4}{W}");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "The Falcon, Sam Wilson")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
