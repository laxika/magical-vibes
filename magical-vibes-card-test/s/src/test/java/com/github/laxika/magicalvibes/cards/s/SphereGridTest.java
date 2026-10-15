package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SphereGrid.class, GrizzlyBears.class})
class SphereGridTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures with +1/+1 counters have reach and trample")
    void counteredCreaturesHaveReachAndTrample() {
        harness.addToBattlefield(player1, new SphereGrid());
        Permanent countered = addCreatureReady(player1, new GrizzlyBears());
        Permanent uncountered = addCreatureReady(player1, new GrizzlyBears());
        countered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, countered, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, countered, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A creature you control gets a +1/+1 counter after dealing combat damage")
    void creatureGetsCounterAfterCombatDamage() {
        harness.addToBattlefield(player1, new SphereGrid());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(java.util.List.of(1));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only your creatures get reach and trample")
    void opposingCreatureDoesNotGetKeywords() {
        harness.addToBattlefield(player1, new SphereGrid());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());
        opposing.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, opposing, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Each creature dealing combat damage gets its own counter")
    void eachCombatDamageDealerGetsCounter() {
        harness.addToBattlefield(player1, new SphereGrid());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent idle = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2));
        resolveCombat();
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(idle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, first, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Opposing combat damage does not trigger Sphere Grid")
    void opposingCombatDamageDoesNotGetCounter() {
        harness.addToBattlefield(player1, new SphereGrid());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Combat damage to a creature does not trigger Sphere Grid")
    void blockedCreatureDoesNotGetCounter() {
        harness.addToBattlefield(player1, new SphereGrid());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(blocker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Multiple Sphere Grids each add a counter")
    void multipleGridsEachAddCounter() {
        harness.addToBattlefield(player1, new SphereGrid());
        harness.addToBattlefield(player1, new SphereGrid());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(2));
        resolveCombat();
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Reach and trample disappear when the last +1/+1 counter is removed")
    void keywordsRequireRemainingPlusOnePlusOneCounter() {
        harness.addToBattlefield(player1, new SphereGrid());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }
}
