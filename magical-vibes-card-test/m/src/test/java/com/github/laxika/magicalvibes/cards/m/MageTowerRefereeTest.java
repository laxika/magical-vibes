package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GloryscaleViashino;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MageTowerReferee.class, GloryscaleViashino.class, GrizzlyBears.class, MycosynthLattice.class})
class MageTowerRefereeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a multicolored spell puts a +1/+1 counter on Mage Tower Referee")
    void multicoloredSpellAddsCounter() {
        harness.addToBattlefield(player1, new MageTowerReferee());
        harness.castFromHand(player1, new GloryscaleViashino(), "{1}{R}{G}{W}");
        harness.passBothPriorities(); // resolve the cast trigger

        Permanent referee = findPermanent(player1, "Mage Tower Referee");
        assertThat(referee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a monocolored spell does not put a counter on Mage Tower Referee")
    void monocoloredSpellDoesNotAddCounter() {
        harness.addToBattlefield(player1, new MageTowerReferee());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent referee = findPermanent(player1, "Mage Tower Referee");
        assertThat(referee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent casting a multicolored spell does not trigger Mage Tower Referee")
    void opponentMulticoloredSpellDoesNotAddCounter() {
        harness.addToBattlefield(player1, new MageTowerReferee());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GloryscaleViashino(), "{1}{R}{G}{W}");
        harness.passBothPriorities();

        Permanent referee = findPermanent(player1, "Mage Tower Referee");
        assertThat(referee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each referee gains one counter for every multicolored spell cast")
    void repeatedCastsAddCountersToEachReferee() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MageTowerReferee());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MageTowerReferee());

        harness.castFromHand(player1, new GloryscaleViashino(), "{1}{R}{G}{W}");
        resolveAllTriggers();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.castFromHand(player1, new GloryscaleViashino(), "{1}{R}{G}{W}");
        resolveAllTriggers();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a colorless spell does not trigger the referee")
    void colorlessSpellDoesNotAddCounter() {
        Permanent referee = harness.addToBattlefieldAndReturn(player1, new MageTowerReferee());

        harness.castFromHand(player1, new MageTowerReferee(), "{2}");
        resolveAllTriggers();

        assertThat(referee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Mage Tower Referee"))
                .allSatisfy(permanent ->
                        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    @DisplayName("Mycosynth Lattice makes a normally multicolored spell colorless")
    void latticePreventsMulticoloredCastTrigger() {
        Permanent referee = harness.addToBattlefieldAndReturn(player1, new MageTowerReferee());
        harness.addToBattlefield(player1, new MycosynthLattice());

        harness.castFromHand(player1, new GloryscaleViashino(), "{1}{R}{G}{W}");
        resolveAllTriggers();

        assertThat(referee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
