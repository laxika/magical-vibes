package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AncestralVision;
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

@CardUsed({Clockspinning.class, AncestralVision.class, GrizzlyBears.class})
class ClockspinningTest extends BaseCardTest {

    @Test
    void addsCounterToTargetPermanent() {
        Permanent target = permanentWithCounter();
        cast(target.getId());

        harness.handleListChoice(player1, "+1/+1 counters");
        harness.handleListChoice(player1, "ADD");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void removesCounterFromTargetPermanent() {
        Permanent target = permanentWithCounter();
        cast(target.getId());

        harness.handleListChoice(player1, "+1/+1 counters");
        harness.handleListChoice(player1, "REMOVE");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void adjustsTimeCounterOnSuspendedCard() {
        AncestralVision target = suspendedCard(2);
        cast(target.getId());

        harness.handleListChoice(player1, "time counters");
        harness.handleListChoice(player1, "ADD");

        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 3);
    }

    @Test
    void removingLastTimeCounterOffersSuspendCast() {
        AncestralVision target = suspendedCard(1);
        cast(target.getId());

        harness.handleListChoice(player1, "time counters");
        harness.handleListChoice(player1, "REMOVE");

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void buybackReturnsClockspinningToHand() {
        Permanent target = permanentWithCounter();
        harness.setHand(player1, List.of(new Clockspinning()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithBuyback(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "+1/+1 counters");
        harness.handleListChoice(player1, "ADD");

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Clockspinning");
    }

    @Test
    void cannotTargetAnUnsuspendedExiledCard() {
        AncestralVision target = new AncestralVision();
        harness.setExile(player2, List.of(target));
        harness.setHand(player1, List.of(new Clockspinning()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("suspended");
    }

    private Permanent permanentWithCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        return target;
    }

    private AncestralVision suspendedCard(int timeCounters) {
        AncestralVision target = new AncestralVision();
        harness.setExile(player2, List.of(target));
        gd.exiledCardTimeCounters.put(target.getId(), timeCounters);
        return target;
    }

    private void cast(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new Clockspinning()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
