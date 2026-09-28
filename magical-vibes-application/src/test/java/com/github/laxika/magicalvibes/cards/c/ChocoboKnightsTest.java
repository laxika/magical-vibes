package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChocoboKnights.class, GrizzlyBears.class})
class ChocoboKnightsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures you control with counters gain double strike")
    void grantsDoubleStrikeToAttackingCreaturesWithCounters() {
        Permanent knights = addCreatureReady(player1, new ChocoboKnights());
        knights.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent counteredAttacker = addCreatureReady(player1, new GrizzlyBears());
        counteredAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent counterlessAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());
        nonattacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, knights, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, counteredAttacker, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, counterlessAttacker, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonattacker, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Granted double strike wears off at end of turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new ChocoboKnights());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
