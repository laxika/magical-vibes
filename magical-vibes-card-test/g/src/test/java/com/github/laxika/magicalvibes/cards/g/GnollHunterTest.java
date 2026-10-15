package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DireWolfProwler;
import com.github.laxika.magicalvibes.cards.y.YoureAmbushedOnTheRoad;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GnollHunter.class, DireWolfProwler.class, YoureAmbushedOnTheRoad.class})
class GnollHunterTest extends BaseCardTest {

    @Test
    void getsCounterWhenAttackingCreaturesHaveTotalPowerAtLeastSix() {
        Permanent hunter = addCreatureReady(player1, new GnollHunter());
        addCreatureReady(player1, new DireWolfProwler());
        addCreatureReady(player1, new DireWolfProwler());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotGetCounterWhenAttackingCreaturesHaveTotalPowerLessThanSix() {
        Permanent hunter = addCreatureReady(player1, new GnollHunter());
        addCreatureReady(player1, new DireWolfProwler());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void packTacticsRequiresGnollHunterToAttack() {
        Permanent hunter = addCreatureReady(player1, new GnollHunter());
        addCreatureReady(player1, new DireWolfProwler());
        addCreatureReady(player1, new DireWolfProwler());
        addCreatureReady(player1, new DireWolfProwler());

        declareAttackers(List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void nonattackingCreaturesDoNotContributePower() {
        Permanent hunter = addCreatureReady(player1, new GnollHunter());
        addCreatureReady(player1, new DireWolfProwler());
        addCreatureReady(player1, new DireWolfProwler());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void eachAttackingHunterGetsItsOwnCounter() {
        Permanent first = addCreatureReady(player1, new GnollHunter());
        Permanent second = addCreatureReady(player1, new GnollHunter());
        Permanent third = addCreatureReady(player1, new GnollHunter());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void counterStillResolvesAfterAnotherAttackerLeaves() {
        Permanent hunter = addCreatureReady(player1, new GnollHunter());
        Permanent supporter = addCreatureReady(player1, new DireWolfProwler());
        addCreatureReady(player1, new DireWolfProwler());
        harness.setHand(player1, List.of(new YoureAmbushedOnTheRoad()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(List.of(0, 1, 2));
        assertThat(gd.stack).hasSize(1);
        harness.castModalInstant(player1, 0, 0, List.of(supporter.getId()));
        resolveAllTriggers();

        harness.assertInHand(player1, "Dire Wolf Prowler");
        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void leavingHunterDoesNotPutCountersOnAnotherHunter() {
        Permanent hunter = addCreatureReady(player1, new GnollHunter());
        Permanent otherHunter = addCreatureReady(player1, new GnollHunter());
        addCreatureReady(player1, new DireWolfProwler());
        addCreatureReady(player1, new DireWolfProwler());
        harness.setHand(player1, List.of(new YoureAmbushedOnTheRoad()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(List.of(0, 2, 3));
        assertThat(gd.stack).hasSize(1);
        harness.castModalInstant(player1, 0, 0, List.of(hunter.getId()));
        resolveAllTriggers();

        harness.assertInHand(player1, "Gnoll Hunter");
        assertThat(otherHunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
