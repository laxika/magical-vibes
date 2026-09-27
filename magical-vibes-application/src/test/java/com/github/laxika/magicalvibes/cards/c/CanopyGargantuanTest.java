package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CanopyGargantuan.class, GrizzlyBears.class})
class CanopyGargantuanTest extends BaseCardTest {

    @Test
    @DisplayName("At upkeep, puts counters equal to each other creature's toughness")
    void putsCountersEqualToEachOtherCreaturesToughness() {
        Permanent gargantuan = addCreatureReady(player1, new CanopyGargantuan());
        Permanent twoTwo = addCreatureReady(player1, new GrizzlyBears());
        Permanent threeThree = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        threeThree.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gargantuan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(twoTwo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(threeThree.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
