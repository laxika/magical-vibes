package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ComfortingCounsel.class, AngelOfMercy.class, GrizzlyBears.class})
class ComfortingCounselTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a growth counter on itself when controller gains life")
    void putsGrowthCounterOnLifeGain() {
        harness.addToBattlefield(player1, new ComfortingCounsel());

        Permanent counsel = findPermanent(player1, "Comforting Counsel");
        assertThat(counsel.getCounterCount(CounterType.GROWTH)).isZero();

        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities(); // resolve Angel of Mercy (ETB gain 3 life)
        harness.passBothPriorities(); // resolve GainLifeEffect
        harness.passBothPriorities(); // resolve Comforting Counsel's triggered ability

        assertThat(counsel.getCounterCount(CounterType.GROWTH)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put growth counters when only the opponent gains life")
    void noCounterWhenOpponentGainsLife() {
        harness.addToBattlefield(player1, new ComfortingCounsel());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities(); // resolve Angel of Mercy
        harness.passBothPriorities(); // resolve GainLifeEffect

        Permanent counsel = findPermanent(player1, "Comforting Counsel");
        assertThat(counsel.getCounterCount(CounterType.GROWTH)).isZero();
    }

    @Test
    @DisplayName("No creature boost with fewer than five growth counters")
    void noBoostBelowThreshold() {
        harness.addToBattlefield(player1, new ComfortingCounsel());
        Permanent counsel = findPermanent(player1, "Comforting Counsel");
        counsel.setCounterCount(CounterType.GROWTH, 4);

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Grants +3/+3 to creatures you control at five or more growth counters")
    void grantsBoostAtThreshold() {
        harness.addToBattlefield(player1, new ComfortingCounsel());
        Permanent counsel = findPermanent(player1, "Comforting Counsel");
        counsel.setCounterCount(CounterType.GROWTH, 5);

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new ComfortingCounsel());
        findPermanent(player1, "Comforting Counsel").setCounterCount(CounterType.GROWTH, 5);

        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gains and loses the anthem as growth counters cross the threshold")
    void boostIsDynamic() {
        harness.addToBattlefield(player1, new ComfortingCounsel());
        Permanent counsel = findPermanent(player1, "Comforting Counsel");
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        counsel.setCounterCount(CounterType.GROWTH, 4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        counsel.setCounterCount(CounterType.GROWTH, 5);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);

        counsel.setCounterCount(CounterType.GROWTH, 3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void separateLifeGainEventsEachAddOneCounterAndActivateBoostOnResolution() {
        Permanent counsel = harness.addToBattlefieldAndReturn(player1, new ComfortingCounsel());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        counsel.setCounterCount(CounterType.GROWTH, 3);

        for (int expectedCounters = 4; expectedCounters <= 5; expectedCounters++) {
            harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
            harness.passBothPriorities();
            harness.passBothPriorities();
            assertThat(counsel.getCounterCount(CounterType.GROWTH)).isEqualTo(expectedCounters - 1);
            assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
            harness.passBothPriorities();
            assertThat(counsel.getCounterCount(CounterType.GROWTH)).isEqualTo(expectedCounters);
        }

        harness.assertLife(player1, 26);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    void multipleCopiesGainCountersIndependentlyAndTheirBoostsStack() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ComfortingCounsel());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ComfortingCounsel());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.GROWTH, 4);
        second.setCounterCount(CounterType.GROWTH, 5);

        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.GROWTH)).isEqualTo(5);
        assertThat(second.getCounterCount(CounterType.GROWTH)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(8);
    }
}
