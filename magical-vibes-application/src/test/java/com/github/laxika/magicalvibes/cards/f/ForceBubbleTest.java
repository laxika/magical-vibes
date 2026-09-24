package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.cards.s.SparkSpray;
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

@CardUsed({ForceBubble.class, GoblinBrigand.class, SparkSpray.class})
class ForceBubbleTest extends BaseCardTest {

    @Test
    @DisplayName("Damage to the controller becomes depletion counters")
    void replacesDamageWithDepletionCounters() {
        Permanent bubble = harness.addToBattlefieldAndReturn(player1, new ForceBubble());
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player2, List.of(new SparkSpray()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(bubble.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Four depletion counters cause Force Bubble to be sacrificed")
    void sacrificesAtFourDepletionCounters() {
        Permanent bubble = harness.addToBattlefieldAndReturn(player1, new ForceBubble());
        harness.setHand(player2, List.of(
                new SparkSpray(), new SparkSpray(), new SparkSpray(), new SparkSpray()));
        harness.addMana(player2, ManaColor.RED, 4);

        for (int i = 0; i < 4; i++) {
            harness.castAndResolveInstant(player2, 0, player1.getId());
        }
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bubble);
    }

    @Test
    @DisplayName("Depletion counters are removed at each end step")
    void removesDepletionCountersAtEndStep() {
        Permanent bubble = harness.addToBattlefieldAndReturn(player1, new ForceBubble());
        bubble.setCounterCount(CounterType.DEPLETION, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bubble);
        assertThat(bubble.getCounterCount(CounterType.DEPLETION)).isZero();
    }

    @Test
    @DisplayName("Combat damage to the controller becomes depletion counters")
    void replacesCombatDamageWithDepletionCounters() {
        Permanent bubble = harness.addToBattlefieldAndReturn(player1, new ForceBubble());
        addCreatureReady(player2, new GoblinBrigand());
        harness.setLife(player1, 20);

        declareAttackers(player2, List.of(0));

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(bubble.getCounterCount(CounterType.DEPLETION)).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage to another player is not replaced")
    void doesNotReplaceDamageToAnotherPlayer() {
        Permanent bubble = harness.addToBattlefieldAndReturn(player1, new ForceBubble());
        harness.setHand(player2, List.of(new SparkSpray()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(bubble.getCounterCount(CounterType.DEPLETION)).isZero();
    }
}
