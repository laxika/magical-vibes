package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MartyrsSoul.class, Plains.class, GrizzlyBears.class})
class MartyrsSoulTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters when you control no tapped lands")
    void entersWithCountersWhenNoLandsAreTapped() {
        Permanent soul = castMartyrsSoul();
        resolveEtb();

        assertThat(soul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not enter with counters when you control a tapped land")
    void doesNotEnterWithCountersWhenLandIsTapped() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        land.tap();

        Permanent soul = castMartyrsSoul();
        resolveEtb();

        assertThat(soul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Convoke can tap a creature to pay for Martyr's Soul")
    void castsWithConvoke() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MartyrsSoul()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
    }

    private Permanent castMartyrsSoul() {
        harness.setHand(player1, List.of(new MartyrsSoul()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Martyr's Soul");
    }

    @Test
    void untappedLandsAndOpponentsTappedLandsDoNotPreventCounters() {
        harness.addToBattlefield(player1, new Plains());
        Permanent opponentsLand = harness.addToBattlefieldAndReturn(player2, new Plains());
        opponentsLand.tap();

        Permanent soul = castMartyrsSoul();
        resolveEtb();

        assertThat(soul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void tappingLandBeforeTriggerResolvesPreventsCounters() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent soul = castMartyrsSoul();
        assertThat(gd.stack).hasSize(1);

        harness.tapPermanent(player1, 0);
        resolveEtb();

        assertThat(land.isTapped()).isTrue();
        assertThat(soul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void noAbilityTriggersWhenLandIsTappedOnEntry() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        land.tap();

        Permanent soul = castMartyrsSoul();

        assertThat(gd.stack).isEmpty();
        assertThat(soul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void convokeCanPayEntireCostAndTappedCreaturesDoNotPreventCounters() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MartyrsSoul());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MartyrsSoul());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new MartyrsSoul());
        harness.setHand(player1, List.of(new MartyrsSoul()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();
        Permanent soul = gd.playerBattlefields.get(player1.getId()).getLast();
        resolveEtb();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(soul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void resolveEtb() {
        harness.passBothPriorities();
    }
}
