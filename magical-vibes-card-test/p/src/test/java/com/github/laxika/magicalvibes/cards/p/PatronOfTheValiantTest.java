package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.t.TopanFreeblade;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PatronOfTheValiant.class, GrizzlyBears.class, SavannahLions.class, TopanFreeblade.class, Plains.class})
class PatronOfTheValiantTest extends BaseCardTest {

    @Test
    @DisplayName("ETB adds a +1/+1 counter only to your creatures that already have one")
    void etbBoostsOnlyCountersCreatures() {
        Permanent withCounter = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        withCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent withoutCounter = harness.addToBattlefieldAndReturn(player1, new SavannahLions());

        castPatron();

        assertThat(withCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(withoutCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("ETB does not touch opponent creatures with +1/+1 counters")
    void etbIgnoresOpponentCreatures() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castPatron();

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Patron itself enters without a counter, so it gets none")
    void patronGetsNoCounter() {
        castPatron();

        assertThat(findPermanent(player1, "Patron of the Valiant").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void addsExactlyOneCounterToEachEligibleCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TopanFreeblade());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TopanFreeblade());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        castPatron();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void ignoresNoncreaturesAndCreaturesWithOnlyOtherCounters() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TopanFreeblade());
        creature.setCounterCount(CounterType.CHARGE, 1);

        castPatron();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void checksCountersAtResolutionAndCanIncludePatronItself() {
        Permanent losesCounter = harness.addToBattlefieldAndReturn(player1, new TopanFreeblade());
        losesCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent gainsCounter = harness.addToBattlefieldAndReturn(player1, new TopanFreeblade());
        harness.setHand(player1, List.of(new PatronOfTheValiant()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent patron = findPermanent(player1, "Patron of the Valiant");
        losesCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        gainsCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        patron.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(losesCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gainsCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(patron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void castPatron() {
        harness.setHand(player1, List.of(new PatronOfTheValiant()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
