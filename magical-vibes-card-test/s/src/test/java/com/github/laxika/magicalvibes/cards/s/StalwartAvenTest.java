package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StalwartAven.class, GiantSpider.class})
class StalwartAvenTest extends BaseCardTest {

    @Test
    @DisplayName("Renown 1 puts a +1/+1 counter on it after unblocked combat damage")
    void renownOnCombatDamage() {
        Permanent aven = addCreatureReady(player1, new StalwartAven());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(aven.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(aven.isRenowned()).isTrue();
    }

    @Test
    @DisplayName("Renown does nothing when the creature is already renowned")
    void renownOnlyOnce() {
        Permanent aven = addCreatureReady(player1, new StalwartAven());
        aven.setRenowned(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(aven.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Renown does not trigger when the creature is blocked")
    void noRenownWhenBlocked() {
        Permanent aven = addCreatureReady(player1, new StalwartAven());
        addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(aven.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(aven.isRenowned()).isFalse();
    }

    @Test
    @DisplayName("Renown waits for its trigger to resolve")
    void renownIsTriggeredRatherThanImmediate() {
        Permanent aven = addCreatureReady(player1, new StalwartAven());
        declareAttackersAndPrepareBlockers(List.of(0));

        harness.resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.stack).hasSize(1);
        assertThat(aven.isRenowned()).isFalse();
        assertThat(aven.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        resolveAllTriggers();

        assertThat(aven.isRenowned()).isTrue();
        assertThat(aven.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Renown rechecks whether the creature is renowned on resolution")
    void alreadyRenownedWhenTriggerResolves() {
        Permanent aven = addCreatureReady(player1, new StalwartAven());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);

        aven.setRenowned(true);
        resolveAllTriggers();

        assertThat(aven.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(aven.isRenowned()).isTrue();
    }

    @Test
    @DisplayName("A zero-power unblocked Aven deals no damage and does not become renowned")
    void noRenownWithoutDamage() {
        Permanent aven = addCreatureReady(player1, new StalwartAven());
        aven.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        declareAttackersAndPrepareBlockers(List.of(0));

        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(aven.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(aven.isRenowned()).isFalse();
    }
}
