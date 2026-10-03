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
    @DisplayName("All creatures you control with counters gain double strike when you attack")
    void grantsDoubleStrikeToAllControlledCreaturesWithCounters() {
        Permanent knights = addCreatureReady(player1, new ChocoboKnights());
        knights.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent counteredAttacker = addCreatureReady(player1, new GrizzlyBears());
        counteredAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent counterlessAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());
        nonattacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0, 1, 2)));
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, knights, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, counteredAttacker, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, counterlessAttacker, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonattacker, Keyword.DOUBLE_STRIKE)).isTrue();
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
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A counter other than a +1/+1 counter qualifies for double strike")
    void grantsDoubleStrikeWithMinusOneMinusOneCounter() {
        Permanent knights = addCreatureReady(player1, new ChocoboKnights());
        knights.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, knights, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Counters are checked when the attack trigger resolves")
    void checksCountersAtResolution() {
        Permanent attacker = addCreatureReady(player1, new ChocoboKnights());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent nonattacker = addCreatureReady(player1, new ChocoboKnights());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).hasSize(2);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        nonattacker.setCounterCount(CounterType.CHARGE, 1);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonattacker, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Removing counters after resolution does not remove granted double strike")
    void grantPersistsAfterCountersAreRemoved() {
        Permanent knights = addCreatureReady(player1, new ChocoboKnights());
        knights.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        knights.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        Permanent newcomer = addCreatureReady(player1, new ChocoboKnights());
        newcomer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, knights, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's attack does not trigger your Chocobo Knights")
    void doesNotTriggerForOpponentsAttack() {
        Permanent knights = addCreatureReady(player1, new ChocoboKnights());
        knights.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent opponent = addCreatureReady(player2, new ChocoboKnights());
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, knights, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.DOUBLE_STRIKE)).isTrue();
    }
}
