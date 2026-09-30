package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FomoriNomad;
import com.github.laxika.magicalvibes.cards.r.RealityStrobe;
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

@CardUsed({DustOfMoments.class, RealityStrobe.class, FomoriNomad.class})
class DustOfMomentsTest extends BaseCardTest {

    @Test
    void removesTwoTimeCountersFromEveryPermanentAndSuspendedCard() {
        Permanent ownPermanent = permanentWithTimeCounters(player1, 3);
        Permanent opposingPermanent = permanentWithTimeCounters(player2, 1);
        Permanent withoutTimeCounters = harness.addToBattlefieldAndReturn(player2, new FomoriNomad());
        RealityStrobe ownSuspended = suspendedCard(player1, 4);
        RealityStrobe opposingSuspended = suspendedCard(player2, 3);

        cast(0);

        assertThat(ownPermanent.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(opposingPermanent.getCounterCount(CounterType.TIME)).isZero();
        assertThat(withoutTimeCounters.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gd.exiledCardTimeCounters)
                .containsEntry(ownSuspended.getId(), 2)
                .containsEntry(opposingSuspended.getId(), 1);
    }

    @Test
    void putsTwoTimeCountersOnPermanentsWithTimeCountersAndSuspendedCards() {
        Permanent withTimeCounters = permanentWithTimeCounters(player1, 1);
        Permanent withoutTimeCounters = harness.addToBattlefieldAndReturn(player2, new FomoriNomad());
        RealityStrobe ownSuspended = suspendedCard(player1, 2);
        RealityStrobe opposingSuspended = suspendedCard(player2, 4);

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
        RealityStrobe suspended = suspendedCard(player2, 1);

        cast(0);

        assertThat(permanent.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(suspended.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void ignoresExiledCardsWithNonSuspendTimeCounters() {
        RealityStrobe suspended = suspendedCard(player1, 3);
        FomoriNomad exiledCardWithNonSuspendCounters = new FomoriNomad();
        harness.setExile(player2, List.of(exiledCardWithNonSuspendCounters));
        gd.exiledCardTimeCounters.put(exiledCardWithNonSuspendCounters.getId(), 4);
        gd.exiledCardsWithNonSuspendTimeCounters.add(exiledCardWithNonSuspendCounters.getId());

        cast(0);

        assertThat(gd.exiledCardTimeCounters)
                .containsEntry(suspended.getId(), 1)
                .containsEntry(exiledCardWithNonSuspendCounters.getId(), 4);
    }

    private Permanent permanentWithTimeCounters(Player player, int count) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new FomoriNomad());
        permanent.setCounterCount(CounterType.TIME, count);
        return permanent;
    }

    private RealityStrobe suspendedCard(Player owner, int timeCounters) {
        RealityStrobe card = new RealityStrobe();
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
