package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AncestralVision;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Timecrafting.class, AncestralVision.class, GrizzlyBears.class})
class TimecraftingTest extends BaseCardTest {

    @Test
    void removesPaidXTimeCountersFromTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.TIME, 5);

        cast(0, 3, target.getId());

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    void addsPaidXTimeCountersToTargetPermanentWithTimeCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.TIME, 1);

        cast(1, 3, target.getId());

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(4);
    }

    @Test
    void adjustsPaidXTimeCountersOnSuspendedCard() {
        AncestralVision target = suspendedCard(4);

        cast(0, 2, target.getId());

        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 2);
    }

    @Test
    void putModeCannotTargetPermanentWithoutTimeCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> cast(1, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private AncestralVision suspendedCard(int timeCounters) {
        AncestralVision target = new AncestralVision();
        harness.setExile(player2, List.of(target));
        gd.exiledCardTimeCounters.put(target.getId(), timeCounters);
        return target;
    }

    private void cast(int mode, int xValue, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new Timecrafting()));
        harness.addMana(player1, ManaColor.RED, xValue + 1);
        harness.castModalInstantForX(player1, 0, mode, xValue, targetId);
        harness.passBothPriorities();
    }
}
