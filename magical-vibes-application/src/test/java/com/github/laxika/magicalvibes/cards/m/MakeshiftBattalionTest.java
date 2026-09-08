package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MakeshiftBattalionTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion puts a +1/+1 counter on Makeshift Battalion")
    void battalionPutsCounterOnSource() {
        Permanent battalion = addCreatureReady(player1, new MakeshiftBattalion());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(battalion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Battalion does not trigger with only one other attacker")
    void battalionDoesNotTriggerWithTooFewAttackers() {
        Permanent battalion = addCreatureReady(player1, new MakeshiftBattalion());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(battalion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
