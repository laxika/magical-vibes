package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeartlessAct.class, GrizzlyBears.class, FountainOfYouth.class})
class HeartlessActTest extends BaseCardTest {

    @Test
    void destroysTargetCreatureWithNoCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void firstModeDoesNothingIfTargetGainsCounterBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(0, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void firstModeCannotTargetCreatureWithCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> cast(0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void secondModeRemovesUpToThreeCountersOfDifferentKinds() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.CHARGE, 2);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        cast(1, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "2");
        harness.handleListChoice(player1, "1");

        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void secondModeCannotTargetNonCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        assertThatThrownBy(() -> cast(1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removesThreeOfFourCountersOfOneKind() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        cast(1, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "3");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void cast(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new HeartlessAct()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, mode, targetId);
    }
}
