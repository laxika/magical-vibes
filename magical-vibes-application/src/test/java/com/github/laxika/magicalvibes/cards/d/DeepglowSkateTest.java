package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeepglowSkate.class, GrizzlyBears.class})
class DeepglowSkateTest extends BaseCardTest {

    @Test
    @DisplayName("ETB doubles every kind of counter on any number of target permanents")
    void doublesCountersOnTargetPermanents() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        first.setCounterCount(CounterType.CHARGE, 3);
        second.setCounterCount(CounterType.LOYALTY, 4);

        castDeepglowSkate(List.of(first.getId(), second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(first.getCounterCount(CounterType.CHARGE)).isEqualTo(6);
        assertThat(second.getCounterCount(CounterType.LOYALTY)).isEqualTo(8);
    }

    @Test
    @DisplayName("ETB can resolve with no targets")
    void canResolveWithoutTargets() {
        castDeepglowSkate(List.of());

        harness.assertOnBattlefield(player1, "Deepglow Skate");
    }

    @Test
    @DisplayName("ETB cannot target a player")
    void cannotTargetPlayer() {
        prepareToCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castDeepglowSkate(List<UUID> targetIds) {
        prepareToCast();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareToCast() {
        harness.setHand(player1, List.of(new DeepglowSkate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
