package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RenderInert.class, GrizzlyBears.class})
class RenderInertTest extends BaseCardTest {

    @Test
    void removesUpToFiveMixedCountersAndDrawsACard() {
        Permanent target = addTargetWithCounters(3, 3);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        cast(target);
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains("+1/+1 counters", "charge counters", "Done");
        harness.handleListChoice(player1, "charge counters");
        harness.handleListChoice(player1, "charge counters");
        harness.handleListChoice(player1, "charge counters");
        harness.handleListChoice(player1, "+1/+1 counters");
        harness.handleListChoice(player1, "+1/+1 counters");

        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void mayRemoveFewerThanFiveCounters() {
        Permanent target = addTargetWithCounters(0, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        cast(target);
        harness.handleListChoice(player1, "charge counters");
        harness.handleListChoice(player1, "Done");

        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void onlyPermanentsCanBeTargeted() {
        harness.setHand(player1, List.of(new RenderInert()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addTargetWithCounters(int plusOneCounters, int chargeCounters) {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, plusOneCounters);
        target.setCounterCount(CounterType.CHARGE, chargeCounters);
        return target;
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new RenderInert()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
