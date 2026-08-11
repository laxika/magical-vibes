package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BenalishLancerTest extends BaseCardTest {

    @Test
    void castWithoutKickerEntersWithoutCountersOrFirstStrike() {
        harness.setHand(player1, List.of(new BenalishLancer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent lancer = findLancer();
        assertThat(lancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void castWithKickerEntersWithTwoCountersAndFirstStrike() {
        harness.setHand(player1, List.of(new BenalishLancer()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent lancer = findLancer();
        assertThat(lancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void kickedFirstStrikeDoesNotWearOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new BenalishLancer()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        Permanent lancer = findLancer();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isTrue();
    }

    private Permanent findLancer() {
        return findPermanent(player1, "Benalish Lancer");
    }
}
