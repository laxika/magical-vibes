package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DuskwalkerTest extends BaseCardTest {

    @Test
    void castWithoutKickerEntersWithoutCountersOrFear() {
        harness.setHand(player1, List.of(new Duskwalker()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent duskwalker = findDuskwalker();
        assertThat(duskwalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, duskwalker, Keyword.FEAR)).isFalse();
    }

    @Test
    void castWithKickerEntersWithTwoCountersAndFear() {
        harness.setHand(player1, List.of(new Duskwalker()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent duskwalker = findDuskwalker();
        assertThat(duskwalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, duskwalker, Keyword.FEAR)).isTrue();
    }

    @Test
    void kickedFearDoesNotWearOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new Duskwalker()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        Permanent duskwalker = findDuskwalker();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, duskwalker, Keyword.FEAR)).isTrue();
    }

    private Permanent findDuskwalker() {
        return findPermanent(player1, "Duskwalker");
    }
}
