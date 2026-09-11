package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForceBubble.class, LightningBolt.class})
class ForceBubbleTest extends BaseCardTest {

    @Test
    @DisplayName("Damage to the controller becomes depletion counters")
    void replacesDamageWithDepletionCounters() {
        Permanent bubble = harness.addToBattlefieldAndReturn(player1, new ForceBubble());
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(bubble.getCounterCount(CounterType.DEPLETION)).isEqualTo(3);
    }

    @Test
    @DisplayName("Four depletion counters cause Force Bubble to be sacrificed")
    void sacrificesAtFourDepletionCounters() {
        Permanent bubble = harness.addToBattlefieldAndReturn(player1, new ForceBubble());
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bubble);
    }

    @Test
    @DisplayName("Depletion counters are removed at each end step")
    void removesDepletionCountersAtEndStep() {
        Permanent bubble = harness.addToBattlefieldAndReturn(player1, new ForceBubble());
        bubble.setCounterCount(CounterType.DEPLETION, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bubble.getCounterCount(CounterType.DEPLETION)).isZero();
    }

    @Test
    @DisplayName("Damage to another player is not replaced")
    void doesNotReplaceDamageToAnotherPlayer() {
        Permanent bubble = harness.addToBattlefieldAndReturn(player1, new ForceBubble());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(bubble.getCounterCount(CounterType.DEPLETION)).isZero();
    }
}
