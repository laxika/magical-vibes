package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LifecraftersGiftTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on the target, then on every qualifying creature you control")
    void putsCountersInOrder() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent alreadyCountered = addCreatureReady(player1, new GrizzlyBears());
        Permanent withoutCounter = addCreatureReady(player1, new GrizzlyBears());
        alreadyCountered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(alreadyCountered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(withoutCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A target controlled by an opponent does not join the controlled-creature sweep")
    void opponentTargetDoesNotQualifyForControlledSweep() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent alreadyCountered = addCreatureReady(player1, new GrizzlyBears());
        alreadyCountered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(alreadyCountered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new LifecraftersGift()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new LifecraftersGift()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
