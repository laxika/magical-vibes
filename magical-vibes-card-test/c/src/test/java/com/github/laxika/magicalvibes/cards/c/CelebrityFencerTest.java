package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CelebrityFencer.class, GrizzlyBears.class})
class CelebrityFencerTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature entering under your control puts a +1/+1 counter on it")
    void anotherCreatureEnteringPutsCounterOnIt() {
        Permanent fencer = harness.enterBattlefieldAndReturn(player1, new CelebrityFencer());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(fencer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's creature entering does not trigger it")
    void opponentCreatureEnteringDoesNotTrigger() {
        Permanent fencer = harness.enterBattlefieldAndReturn(player1, new CelebrityFencer());

        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(fencer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Celebrity Fencer's own entry does not trigger it")
    void ownEntryDoesNotTrigger() {
        Permanent fencer = harness.enterBattlefieldAndReturn(player1, new CelebrityFencer());
        resolveAllTriggers();

        assertThat(fencer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Fencer puts counters on itself when another Fencer enters")
    void multipleFencersPutCountersOnTheirOwnSources() {
        Permanent first = harness.enterBattlefieldAndReturn(player1, new CelebrityFencer());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new CelebrityFencer());
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        Permanent third = harness.enterBattlefieldAndReturn(player1, new CelebrityFencer());
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Every allied creature entry triggers even before earlier triggers resolve")
    void repeatedEntriesAccumulateCounters() {
        Permanent fencer = harness.enterBattlefieldAndReturn(player1, new CelebrityFencer());

        harness.enterBattlefieldAndReturn(player1, new CelebrityFencer());
        harness.enterBattlefieldAndReturn(player1, new CelebrityFencer());
        assertThat(fencer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        resolveAllTriggers();

        assertThat(fencer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
