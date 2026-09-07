package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AncestralVision;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DustOfMoments.class, AncestralVision.class, GrizzlyBears.class})
class DustOfMomentsTest extends BaseCardTest {

    @Test
    void removesTwoTimeCountersFromEveryPermanentAndSuspendedCard() {
        Permanent ownPermanent = permanentWithTimeCounters(player1, 3);
        Permanent opposingPermanent = permanentWithTimeCounters(player2, 1);
        AncestralVision ownSuspended = suspendedCard(player1, 4);
        AncestralVision opposingSuspended = suspendedCard(player2, 3);

        cast(0);

        assertThat(ownPermanent.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(opposingPermanent.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gd.exiledCardTimeCounters)
                .containsEntry(ownSuspended.getId(), 2)
                .containsEntry(opposingSuspended.getId(), 1);
    }

    @Test
    void putsTwoTimeCountersOnPermanentsWithTimeCountersAndSuspendedCards() {
        Permanent withTimeCounters = permanentWithTimeCounters(player1, 1);
        Permanent withoutTimeCounters = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        AncestralVision ownSuspended = suspendedCard(player1, 2);
        AncestralVision opposingSuspended = suspendedCard(player2, 4);

        cast(1);

        assertThat(withTimeCounters.getCounterCount(CounterType.TIME)).isEqualTo(3);
        assertThat(withoutTimeCounters.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gd.exiledCardTimeCounters)
                .containsEntry(ownSuspended.getId(), 4)
                .containsEntry(opposingSuspended.getId(), 6);
    }

    @Test
    void removingLastTimeCounterFromSuspendedCardOffersItsSuspendCast() {
        Permanent permanent = permanentWithTimeCounters(player1, 3);
        AncestralVision suspended = suspendedCard(player2, 1);

        cast(0);

        assertThat(permanent.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(suspended.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private Permanent permanentWithTimeCounters(Player player, int count) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setCounterCount(CounterType.TIME, count);
        return permanent;
    }

    private AncestralVision suspendedCard(Player owner, int timeCounters) {
        AncestralVision card = new AncestralVision();
        harness.setExile(owner, List.of(card));
        gd.exiledCardTimeCounters.put(card.getId(), timeCounters);
        return card;
    }

    private void cast(int mode) {
        harness.setHand(player1, List.of(new DustOfMoments()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castModalInstant(player1, 0, mode, List.of());
        harness.passBothPriorities();
    }
}
