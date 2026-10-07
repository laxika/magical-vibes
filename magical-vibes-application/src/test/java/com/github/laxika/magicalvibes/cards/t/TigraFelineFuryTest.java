package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TigraFelineFury.class)
class TigraFelineFuryTest extends BaseCardTest {

    @Test
    void getsCounterWhenControllerGainsLife() {
        Permanent tigra = harness.addToBattlefieldAndReturn(player1, new TigraFelineFury());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        harness.passBothPriorities();

        assertThat(tigra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerWhenOpponentGainsLife() {
        Permanent tigra = harness.addToBattlefieldAndReturn(player1, new TigraFelineFury());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 1));

        assertThat(tigra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void gainsOnlyOneCounterForALargerLifeGain() {
        Permanent tigra = harness.addToBattlefieldAndReturn(player1, new TigraFelineFury());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 5));
        harness.passBothPriorities();

        assertThat(tigra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void gainsACounterForEachSeparateLifeGainInTheSameTurn() {
        Permanent tigra = harness.addToBattlefieldAndReturn(player1, new TigraFelineFury());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2));
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.passBothPriorities();

        assertThat(tigra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerForZeroLifeGain() {
        Permanent tigra = harness.addToBattlefieldAndReturn(player1, new TigraFelineFury());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 0));
        harness.passBothPriorities();

        assertThat(tigra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
