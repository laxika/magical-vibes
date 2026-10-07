package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TyranidPrime.class, GrizzlyBears.class, HillGiant.class, TrygonPrime.class})
class TyranidPrimeTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control have evolve")
    void grantsEvolveToOtherCreaturesOnly() {
        Permanent prime = harness.addToBattlefieldAndReturn(player1, new TyranidPrime());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, prime, Keyword.EVOLVE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.EVOLVE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.EVOLVE)).isFalse();
    }

    @Test
    @DisplayName("Granted evolve puts a +1/+1 counter on the other creature")
    void grantedEvolveTriggers() {
        Permanent prime = harness.addToBattlefieldAndReturn(player1, new TyranidPrime());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        resolveAllTriggers();

        assertThat(prime.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Prime's printed and granted evolve trigger separately")
    void multipleInstancesOfEvolveTriggerSeparately() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TyranidPrime());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TyranidPrime());

        harness.castFromHand(player1, new TrygonPrime(), "{2}{G}{U}");
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tyranid Prime grants evolve in time for its own entry to trigger it")
    void enteringPrimeTriggersNewlyGrantedEvolveFromGreaterToughness() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new TyranidPrime(), "{1}{G}{U}");
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equal power and toughness do not trigger evolve")
    void equalStatsDoNotTriggerEvolve() {
        Permanent prime = harness.addToBattlefieldAndReturn(player1, new TyranidPrime());

        harness.castFromHand(player1, new TyranidPrime(), "{1}{G}{U}");
        resolveAllTriggers();

        assertThat(prime.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each evolve instance rechecks the comparison on resolution")
    void repeatedEvolveStopsWhenStatsBecomeEqual() {
        harness.addToBattlefield(player1, new TyranidPrime());
        harness.addToBattlefield(player1, new TyranidPrime());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's creature entering does not trigger evolve")
    void opponentEntryDoesNotTriggerEvolve() {
        Permanent prime = harness.addToBattlefieldAndReturn(player1, new TyranidPrime());

        harness.enterBattlefieldAndReturn(player2, new TrygonPrime());
        resolveAllTriggers();

        assertThat(prime.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
