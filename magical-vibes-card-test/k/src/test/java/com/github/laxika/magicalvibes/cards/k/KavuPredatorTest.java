package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.h.HeroesRemembered;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KavuPredator.class, HeroesRemembered.class})
class KavuPredatorTest extends BaseCardTest {

    @Test
    @DisplayName("Gets as many +1/+1 counters as life gained by an opponent")
    void getsCountersEqualToOpponentsLifeGain() {
        Permanent predator = harness.addToBattlefieldAndReturn(player1, new KavuPredator());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new HeroesRemembered(), "{6}{W}{W}{W}");
        resolveAllTriggers();

        assertThat(predator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger when its controller gains life")
    void doesNotTriggerForControllerLifeGain() {
        Permanent predator = harness.addToBattlefieldAndReturn(player1, new KavuPredator());

        harness.castFromHand(player1, new HeroesRemembered(), "{6}{W}{W}{W}");
        resolveAllTriggers();

        assertThat(predator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Accumulates counters for each separate opponent life gain")
    void accumulatesCountersAcrossLifeGainEvents() {
        Permanent predator = harness.addToBattlefieldAndReturn(player1, new KavuPredator());
        harness.setLife(player2, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new HeroesRemembered(), "{6}{W}{W}{W}");
        resolveAllTriggers();
        assertThat(predator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(20);
        harness.assertLife(player2, 21);

        harness.castFromHand(player2, new HeroesRemembered(), "{6}{W}{W}{W}");
        resolveAllTriggers();
        assertThat(predator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(40);
        harness.assertLife(player2, 41);
    }

    @Test
    @DisplayName("Each opposing Predator gets counters, but the gaining player's Predator does not")
    void eachPredatorChecksItsOwnController() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new KavuPredator());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new KavuPredator());
        Permanent own = harness.addToBattlefieldAndReturn(player2, new KavuPredator());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new HeroesRemembered(), "{6}{W}{W}{W}");
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(20);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(20);
        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
