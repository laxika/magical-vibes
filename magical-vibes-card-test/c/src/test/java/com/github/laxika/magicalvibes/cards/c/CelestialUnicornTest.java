package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.PotionOfHealing;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CelestialUnicorn.class, PotionOfHealing.class})
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

    @Test
    @DisplayName("Separate life gains each put one counter on Celestial Unicorn")
    void separateLifeGainsEachPutCounter() {
        Permanent unicorn = harness.addToBattlefieldAndReturn(player1, new CelestialUnicorn());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 5));
        harness.passBothPriorities();

        assertThat(unicorn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Gaining zero life does not trigger Celestial Unicorn")
    void gainingZeroLifeDoesNotPutCounter() {
        Permanent unicorn = harness.addToBattlefieldAndReturn(player1, new CelestialUnicorn());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 0));

        assertThat(gd.stack).isEmpty();
        assertThat(unicorn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each Celestial Unicorn gets its own counter from a life gain")
    void eachUnicornGetsItsOwnCounter() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CelestialUnicorn());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CelestialUnicorn());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Potion of Healing creates a separate trigger that resolves after gaining life")
    void potionLifeGainTriggersCounterOnResolution() {
        Permanent unicorn = harness.addToBattlefieldAndReturn(player1, new CelestialUnicorn());
        harness.addToBattlefield(player1, new PotionOfHealing());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(unicorn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(unicorn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
