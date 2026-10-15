package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BasrisSolidarity.class, AlpineWatchdog.class, GloriousAnthem.class})
class BasrisSolidarityTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each creature you control")
    void putsCounterOnEachCreatureYouControl() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        Permanent ownNoncreature = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());

        harness.setHand(player1, List.of(new BasrisSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(firstBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownNoncreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Resolves with no creatures and leaves opposing creatures unchanged")
    void resolvesWithNoControlledCreatures() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        harness.setHand(player1, List.of(new BasrisSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof BasrisSolidarity);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Adds to existing counters and affects only creatures present at resolution")
    void usesBattlefieldAtResolutionAndAddsToExistingCounters() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        existing.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new BasrisSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, 0);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());

        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(beforeResolution.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(afterResolution.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
