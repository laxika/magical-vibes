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
}
