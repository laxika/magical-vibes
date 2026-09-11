package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CelestialUnicorn.class)
class CelestialUnicornTest extends BaseCardTest {

    @Test
    @DisplayName("Gaining life puts one +1/+1 counter on Celestial Unicorn")
    void gainingLifePutsCounter() {
        Permanent unicorn = harness.addToBattlefieldAndReturn(player1, new CelestialUnicorn());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.passBothPriorities();

        assertThat(unicorn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("An opponent gaining life does not trigger Celestial Unicorn")
    void opponentGainingLifeDoesNotPutCounter() {
        Permanent unicorn = harness.addToBattlefieldAndReturn(player1, new CelestialUnicorn());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));
        harness.passBothPriorities();

        assertThat(unicorn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
