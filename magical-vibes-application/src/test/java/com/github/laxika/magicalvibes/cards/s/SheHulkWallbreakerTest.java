package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ExtremisElite;
import com.github.laxika.magicalvibes.cards.x.X23DeadlyWeapon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SheHulkWallbreaker.class, ExtremisElite.class, X23DeadlyWeapon.class})
class SheHulkWallbreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Other Heroes you control have trample")
    void grantsTrampleToOtherHeroesYouControl() {
        addCreatureReady(player1, new SheHulkWallbreaker());
        Permanent ownHero = addCreatureReady(player1, new X23DeadlyWeapon());
        Permanent ownNonHero = addCreatureReady(player1, new ExtremisElite());
        Permanent opposingHero = addCreatureReady(player2, new X23DeadlyWeapon());

        assertThat(gqs.hasKeyword(gd, ownHero, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownNonHero, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingHero, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A Hero becoming blocked gets one +1/+1 counter per blocker")
    void putsCountersEqualToBlockersOnBecomingBlocked() {
        Permanent sheHulk = addCreatureReady(player1, new SheHulkWallbreaker());
        Permanent hero = addCreatureReady(player1, new X23DeadlyWeapon());
        addCreatureReady(player2, new ExtremisElite());
        addCreatureReady(player2, new ExtremisElite());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 1)));
        harness.passBothPriorities();

        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(sheHulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("She-Hulk also receives counters when she becomes blocked")
    void putsCountersOnSheHulkHerself() {
        Permanent sheHulk = addCreatureReady(player1, new SheHulkWallbreaker());
        addCreatureReady(player2, new ExtremisElite());
        addCreatureReady(player2, new ExtremisElite());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(sheHulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A blocked non-Hero does not trigger She-Hulk")
    void doesNotPutCountersOnNonHero() {
        Permanent sheHulk = addCreatureReady(player1, new SheHulkWallbreaker());
        Permanent nonHero = addCreatureReady(player1, new ExtremisElite());
        addCreatureReady(player2, new ExtremisElite());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gd.stack).isEmpty();
        assertThat(nonHero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(sheHulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opposing Hero becoming blocked does not trigger She-Hulk")
    void doesNotPutCountersOnOpposingHero() {
        Permanent sheHulk = addCreatureReady(player1, new SheHulkWallbreaker());
        addCreatureReady(player1, new ExtremisElite());
        Permanent hero = addCreatureReady(player2, new X23DeadlyWeapon());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(sheHulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
