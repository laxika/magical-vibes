package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FomoriNomad;
import com.github.laxika.magicalvibes.cards.l.LostAuramancers;
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

@CardUsed({DustOfMoments.class, RealityStrobe.class, FomoriNomad.class, LostAuramancers.class})
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
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
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

    @Test
    void removingLastTimeCountersTriggersVanishingSacrifice() {
        Permanent auramancers = harness.addToBattlefieldAndReturn(player2, new LostAuramancers());
        auramancers.setCounterCount(CounterType.TIME, 2);

        cast(0);
        resolveAllTriggers();

        assertThat(auramancers.getCounterCount(CounterType.TIME)).isZero();
        harness.assertNotOnBattlefield(player2, "Lost Auramancers");
        harness.assertInGraveyard(player2, "Lost Auramancers");
    }

    @Test
    void addingCountersAffectsOpposingPermanentsAndPreservesOtherCounterTypes() {
        Permanent opposingPermanent = permanentWithTimeCounters(player2, 2);
        opposingPermanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent withoutTimeCounters = harness.addToBattlefieldAndReturn(player1, new FomoriNomad());
        withoutTimeCounters.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        cast(1);

        assertThat(opposingPermanent.getCounterCount(CounterType.TIME)).isEqualTo(4);
        assertThat(opposingPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(withoutTimeCounters.getCounterCount(CounterType.TIME)).isZero();
        assertThat(withoutTimeCounters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void addingCountersIgnoresNonSuspendCountersAndExiledSuspendCardsWithoutTimeCounters() {
        RealityStrobe withoutTimeCounters = suspendedCard(player1, 0);
        FomoriNomad nonSuspended = new FomoriNomad();
        harness.setExile(player2, List.of(nonSuspended));
        gd.exiledCardTimeCounters.put(nonSuspended.getId(), 3);
        gd.exiledCardsWithNonSuspendTimeCounters.add(nonSuspended.getId());

        cast(1);

        assertThat(gd.exiledCardTimeCounters)
                .containsEntry(withoutTimeCounters.getId(), 0)
                .containsEntry(nonSuspended.getId(), 3);
        assertThat(gd.findExiledCard(withoutTimeCounters.getId())).isNotNull();
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
