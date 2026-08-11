package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PouncingKavuTest extends BaseCardTest {

    @Test
    void castWithoutKickerEntersWithoutCountersOrHaste() {
        harness.setHand(player1, List.of(new PouncingKavu()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent pouncingKavu = findPouncingKavu();
        assertThat(pouncingKavu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, pouncingKavu, Keyword.HASTE)).isFalse();
    }

    @Test
    void castWithKickerEntersWithTwoCountersAndHaste() {
        harness.setHand(player1, List.of(new PouncingKavu()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent pouncingKavu = findPouncingKavu();
        assertThat(pouncingKavu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, pouncingKavu, Keyword.HASTE)).isTrue();
    }

    private Permanent findPouncingKavu() {
        return findPermanent(player1, "Pouncing Kavu");
    }
}
