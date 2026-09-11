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
        harness.addToBattlefield(player1, new TigraFelineFury());
        Permanent tigra = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        harness.passBothPriorities();

        assertThat(tigra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerWhenOpponentGainsLife() {
        harness.addToBattlefield(player1, new TigraFelineFury());
        Permanent tigra = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 1));

        assertThat(tigra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
