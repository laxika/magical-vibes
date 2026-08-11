package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KavuMonarchTest extends BaseCardTest {

    @Test
    void allKavuCreaturesHaveTrampleIncludingKavuMonarch() {
        Permanent monarch = harness.addToBattlefieldAndReturn(player1, new KavuMonarch());
        Permanent alliedKavu = harness.addToBattlefieldAndReturn(player1, new KavuAggressor());
        Permanent opposingKavu = harness.addToBattlefieldAndReturn(player2, new KavuAggressor());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, monarch, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, alliedKavu, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingKavu, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void getsCounterWhenAnotherKavuEnters() {
        Permanent monarch = harness.addToBattlefieldAndReturn(player1, new KavuMonarch());

        castKavu(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(monarch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void getsCounterWhenOpponentsKavuEnters() {
        Permanent monarch = harness.addToBattlefieldAndReturn(player1, new KavuMonarch());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castKavu(player2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(monarch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForNonKavuOrItself() {
        Permanent monarch = harness.addToBattlefieldAndReturn(player1, new KavuMonarch());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(monarch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.setHand(player1, List.of(new KavuMonarch()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(monarch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castKavu(Player player) {
        harness.setHand(player, List.of(new KavuAggressor()));
        harness.addMana(player, ManaColor.RED, 3);
        harness.castCreature(player, 0);
    }
}
