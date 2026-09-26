package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.m.MetathranZombie;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KavuMonarch.class, KavuAggressor.class, MetathranZombie.class})
class KavuMonarchTest extends BaseCardTest {

    @Test
    void allKavuCreaturesHaveTrampleIncludingKavuMonarch() {
        Permanent monarch = harness.addToBattlefieldAndReturn(player1, new KavuMonarch());
        Permanent alliedKavu = harness.addToBattlefieldAndReturn(player1, new KavuAggressor());
        Permanent opposingKavu = harness.addToBattlefieldAndReturn(player2, new KavuAggressor());
        Permanent nonKavu = harness.addToBattlefieldAndReturn(player1, new MetathranZombie());

        assertThat(gqs.hasKeyword(gd, monarch, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, alliedKavu, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingKavu, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonKavu, Keyword.TRAMPLE)).isFalse();
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
    void doesNotTriggerForNonKavu() {
        Permanent monarch = harness.addToBattlefieldAndReturn(player1, new KavuMonarch());

        harness.castFromHand(player1, new MetathranZombie(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(monarch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerOnItsOwnEntryButExistingMonarchDoes() {
        Permanent monarch = harness.addToBattlefieldAndReturn(player1, new KavuMonarch());

        harness.castFromHand(player1, new KavuMonarch(), "{2}{R}{R}");
        harness.passBothPriorities();
        Permanent enteringMonarch = findPermanents(player1, "Kavu Monarch").get(1);
        harness.passBothPriorities();

        assertThat(monarch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(enteringMonarch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castKavu(Player player) {
        harness.castFromHand(player, new KavuAggressor(), "{2}{R}");
    }
}
