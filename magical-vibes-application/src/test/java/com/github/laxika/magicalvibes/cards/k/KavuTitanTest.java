package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KavuTitanTest extends BaseCardTest {

    @Test
    void castWithoutKickerEntersWithoutCountersOrTrample() {
        harness.setHand(player1, List.of(new KavuTitan()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent kavuTitan = findKavuTitan();
        assertThat(kavuTitan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, kavuTitan, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void castWithKickerEntersWithThreeCountersAndTrample() {
        harness.setHand(player1, List.of(new KavuTitan()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent kavuTitan = findKavuTitan();
        assertThat(kavuTitan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, kavuTitan, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent findKavuTitan() {
        return findPermanent(player1, "Kavu Titan");
    }
}
