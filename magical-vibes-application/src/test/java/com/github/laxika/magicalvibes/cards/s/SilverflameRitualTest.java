package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilverflameRitual.class, GrizzlyBears.class})
class SilverflameRitualTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each creature you control without Adamant")
    void putsCountersWithoutAdamant() {
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());

        castWithMana(1, 3);

        assertThat(firstBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(secondBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, firstBear, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Adamant grants vigilance after putting counters on your creatures")
    void adamantGrantsVigilance() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        castWithMana(4, 0);

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();
    }

    private void castWithMana(int whiteMana, int colorlessMana) {
        harness.setHand(player1, List.of(new SilverflameRitual()));
        harness.addMana(player1, ManaColor.WHITE, whiteMana);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
