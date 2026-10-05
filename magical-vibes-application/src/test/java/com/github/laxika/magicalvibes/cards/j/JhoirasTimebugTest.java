package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AncestralVision;
import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JhoirasTimebug.class, AncestralVision.class, AshcoatBear.class})
class JhoirasTimebugTest extends BaseCardTest {

    @Test
    void addsCounterToControlledPermanent() {
        Permanent timebug = timebug();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        target.setCounterCount(CounterType.TIME, 1);

        activate(timebug, target.getId());

        harness.handleListChoice(player1, "time counters");
        harness.handleListChoice(player1, "ADD");

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    void removesCounterFromControlledPermanent() {
        Permanent timebug = timebug();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        target.setCounterCount(CounterType.TIME, 1);

        activate(timebug, target.getId());

        harness.handleListChoice(player1, "time counters");
        harness.handleListChoice(player1, "REMOVE");

        assertThat(target.getCounterCount(CounterType.TIME)).isZero();
    }

    @Test
    void doesNothingForPermanentWithoutTimeCounter() {
        Permanent timebug = timebug();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        activate(timebug, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void adjustsTimeCounterOnOwnedSuspendedCard() {
        Permanent timebug = timebug();
        AncestralVision target = suspendedCard(player1, 2);

        activate(timebug, target.getId(), Zone.EXILE);

        harness.handleListChoice(player1, "time counters");
        harness.handleListChoice(player1, "ADD");

        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 3);
    }

    @Test
    void removingLastTimeCounterOffersSuspendCast() {
        Permanent timebug = timebug();
        AncestralVision target = suspendedCard(player1, 1);

        activate(timebug, target.getId(), Zone.EXILE);

        harness.handleListChoice(player1, "time counters");
        harness.handleListChoice(player1, "REMOVE");

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void cannotTargetOpponentPermanent() {
        Permanent timebug = timebug();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        target.setCounterCount(CounterType.TIME, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(timebug), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetSuspendedCardOwnedByOpponent() {
        Permanent timebug = timebug();
        AncestralVision target = suspendedCard(player2, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(timebug), null, target.getId(), Zone.EXILE))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayDeclineToAdjustTimeCounter() {
        Permanent timebug = timebug();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        target.setCounterCount(CounterType.TIME, 1);

        activate(timebug, target.getId());

        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNothingWhenTimeCounterIsRemovedBeforeResolution() {
        Permanent timebug = timebug();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        target.setCounterCount(CounterType.TIME, 1);

        harness.activateAbility(player1, battlefieldIndex(timebug), null, target.getId());
        target.setCounterCount(CounterType.TIME, 0);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(timebug.isTapped()).isTrue();
    }

    @Test
    void cannotTargetExiledCardWithoutTimeCounters() {
        Permanent timebug = timebug();
        AncestralVision target = new AncestralVision();
        harness.setExile(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(timebug), null, target.getId(), Zone.EXILE))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent timebug() {
        return addCreatureReady(player1, new JhoirasTimebug());
    }

    private AncestralVision suspendedCard(Player owner, int timeCounters) {
        AncestralVision target = new AncestralVision();
        harness.setExile(owner, List.of(target));
        gd.exiledCardTimeCounters.put(target.getId(), timeCounters);
        return target;
    }

    private void activate(Permanent timebug, java.util.UUID targetId) {
        harness.activateAbility(player1, battlefieldIndex(timebug), null, targetId);
        harness.passBothPriorities();
    }

    private void activate(Permanent timebug, java.util.UUID targetId, Zone zone) {
        harness.activateAbility(player1, battlefieldIndex(timebug), null, targetId, zone);
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
