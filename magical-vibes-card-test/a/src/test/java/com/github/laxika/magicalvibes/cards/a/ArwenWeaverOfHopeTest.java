package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArwenWeaverOfHope.class, GrizzlyBears.class, ElvishMystic.class, Lignify.class})
class ArwenWeaverOfHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control enter with counters equal to Arwen's toughness")
    void otherCreatureEntersWithCountersEqualToArwensToughness() {
        Permanent arwen = harness.addToBattlefieldAndReturn(player1, new ArwenWeaverOfHope());
        arwen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Arwen does not give herself entry counters")
    void arwenDoesNotGiveHerselfEntryCounters() {
        Permanent arwen = harness.enterBattlefieldAndReturn(player1, new ArwenWeaverOfHope());

        assertThat(arwen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Arwen does not affect creatures entering under an opponent's control")
    void doesNotAffectOpponentsCreatures() {
        harness.addToBattlefield(player1, new ArwenWeaverOfHope());

        Permanent bears = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Arwen's entry replacement stops applying when she loses her abilities")
    void losingAbilitiesStopsEntryCounters() {
        Permanent arwen = harness.addToBattlefieldAndReturn(player1, new ArwenWeaverOfHope());
        Permanent lignify = harness.addToBattlefieldAndReturn(player1, new Lignify());
        lignify.setAttachedTo(arwen.getId());

        Permanent mystic = harness.enterBattlefieldAndReturn(player1, new ElvishMystic());

        assertThat(mystic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Marked damage does not reduce the toughness used for entry counters")
    void markedDamageDoesNotReduceEntryCounters() {
        Permanent arwen = harness.addToBattlefieldAndReturn(player1, new ArwenWeaverOfHope());
        arwen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        arwen.setMarkedDamage(2);

        Permanent mystic = harness.enterBattlefieldAndReturn(player1, new ElvishMystic());

        assertThat(mystic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each entry uses Arwen's current toughness without changing earlier counters")
    void successiveEntriesUseCurrentToughness() {
        Permanent arwen = harness.addToBattlefieldAndReturn(player1, new ArwenWeaverOfHope());
        Permanent first = harness.enterBattlefieldAndReturn(player1, new ElvishMystic());
        arwen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        Permanent second = harness.enterBattlefieldAndReturn(player1, new ElvishMystic());

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }
}
