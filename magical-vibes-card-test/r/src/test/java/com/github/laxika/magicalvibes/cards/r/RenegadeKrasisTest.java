package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.t.ThrashingMossdog;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RenegadeKrasis.class, AirElemental.class, GrizzlyBears.class, HillGiant.class, ThrashingMossdog.class})
class RenegadeKrasisTest extends BaseCardTest {

    @Test
    @DisplayName("Evolving puts a +1/+1 counter on each other creature you control that has one")
    void evolveTriggerBoostsOtherCounterBearers() {
        Permanent krasis = harness.addToBattlefieldAndReturn(player1, new RenegadeKrasis());
        Permanent countered = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent uncountered = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentCountered = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        countered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opponentCountered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.castFromHand(player1, new AirElemental(), "{3}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(krasis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(uncountered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCountered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No evolve, no trigger")
    void noEvolveNoTrigger() {
        Permanent krasis = harness.addToBattlefieldAndReturn(player1, new RenegadeKrasis());
        Permanent countered = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        countered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(krasis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Greater toughness alone causes evolve")
    void greaterToughnessCausesEvolve() {
        Permanent krasis = harness.addToBattlefieldAndReturn(player1, new RenegadeKrasis());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new RenegadeKrasis());
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.castFromHand(player1, new ThrashingMossdog(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(krasis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Evolve rechecks creature sizes when resolving")
    void evolveRechecksSizes() {
        Permanent krasis = harness.addToBattlefieldAndReturn(player1, new RenegadeKrasis());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new RenegadeKrasis());
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.castFromHand(player1, new ThrashingMossdog(), "{3}{G}");
        harness.passBothPriorities();
        krasis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(krasis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The evolves ability checks counter bearers when it resolves")
    void counterEligibilityCheckedAtResolution() {
        Permanent krasis = harness.addToBattlefieldAndReturn(player1, new RenegadeKrasis());
        Permanent gainsCounter = harness.addToBattlefieldAndReturn(player1, new ThrashingMossdog());
        Permanent losesCounter = harness.addToBattlefieldAndReturn(player1, new ThrashingMossdog());
        losesCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.castFromHand(player1, new ThrashingMossdog(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        gainsCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        losesCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(krasis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gainsCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(losesCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
