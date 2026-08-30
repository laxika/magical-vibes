package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RumblingRuinTest extends BaseCardTest {

    @Test
    @DisplayName("Opposing creatures with power at most the controlled +1/+1 counter count can't block")
    void opposingCreaturesAtOrBelowCounterCountCantBlock() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent ownCreatureWithCounters = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        ownCreatureWithCounters.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        Permanent opposingSmallCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opposingLargeCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opposingLargeCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castRumblingRuin();

        assertThat(ownCreature.isCantBlockThisTurn()).isFalse();
        assertThat(ownCreatureWithCounters.isCantBlockThisTurn()).isFalse();
        assertThat(opposingSmallCreature.isCantBlockThisTurn()).isTrue();
        assertThat(opposingLargeCreature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("No controlled +1/+1 counters leave positive-power opposing creatures able to block")
    void noCountersDoNotAffectPositivePowerCreatures() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        castRumblingRuin();

        assertThat(opposingCreature.isCantBlockThisTurn()).isFalse();
    }

    private void castRumblingRuin() {
        harness.setHand(player1, List.of(new RumblingRuin()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
