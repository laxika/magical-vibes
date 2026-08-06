package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RubblebeltRaidersTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alone puts a single +1/+1 counter on it (it counts itself)")
    void attackingAloneAddsOneCounter() {
        Permanent raiders = addCreatureReady(player1, new RubblebeltRaiders());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(raiders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(raiders.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts every attacking creature you control, and only the Raiders get counters")
    void countsAllAttackersYouControl() {
        Permanent raiders = addCreatureReady(player1, new RubblebeltRaiders());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(raiders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Non-attacking creatures you control are not counted")
    void ignoresNonAttackingCreatures() {
        Permanent raiders = addCreatureReady(player1, new RubblebeltRaiders());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(raiders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures the opponent controls are not counted")
    void ignoresOpponentCreatures() {
        Permanent raiders = addCreatureReady(player1, new RubblebeltRaiders());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(raiders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
