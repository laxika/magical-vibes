package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AeonChronicler;
import com.github.laxika.magicalvibes.cards.a.AetherMembrane;
import com.github.laxika.magicalvibes.cards.d.Delay;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Timecrafting.class, AeonChronicler.class, AetherMembrane.class, Delay.class})
class TimecraftingTest extends BaseCardTest {

    @Test
    void removesPaidXTimeCountersFromTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AetherMembrane());
        target.setCounterCount(CounterType.TIME, 5);

        cast(0, 3, target.getId());

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    void addsPaidXTimeCountersToTargetPermanentWithTimeCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AetherMembrane());
        target.setCounterCount(CounterType.TIME, 1);

        cast(1, 3, target.getId());

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(4);
    }

    @Test
    void adjustsPaidXTimeCountersOnSuspendedCard() {
        AeonChronicler target = suspendedCard(4);

        cast(0, 2, target.getId());

        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 2);
    }

    @Test
    void putsPaidXTimeCountersOnSuspendedCard() {
        AeonChronicler target = suspendedCard(1);

        cast(1, 3, target.getId());

        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 4);
    }

    @Test
    void putModeCannotTargetPermanentWithoutTimeCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AetherMembrane());

        assertThatThrownBy(() -> cast(1, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetExiledCardWithNonSuspendTimeCounters() {
        AetherMembrane target = new AetherMembrane();
        harness.setExile(player2, List.of(target));
        gd.exiledCardTimeCounters.put(target.getId(), 3);
        gd.exiledCardsWithNonSuspendTimeCounters.add(target.getId());

        assertThatThrownBy(() -> cast(0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removeModeCanTargetPermanentWithoutTimeCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AetherMembrane());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        cast(0, 3, target.getId());

        assertThat(target.getCounterCount(CounterType.TIME)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Timecrafting");
    }

    @Test
    void removesOnlyAvailableTimeCountersWhenXIsLarger() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AetherMembrane());
        target.setCounterCount(CounterType.TIME, 2);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        cast(0, 5, target.getId());

        assertThat(target.getCounterCount(CounterType.TIME)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void zeroXDoesNotRemoveTimeCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AetherMembrane());
        target.setCounterCount(CounterType.TIME, 2);

        cast(0, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Timecrafting");
    }

    @Test
    void zeroXDoesNotAddTimeCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AetherMembrane());
        target.setCounterCount(CounterType.TIME, 2);

        cast(1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Timecrafting");
    }

    @Test
    void putModeDoesNotResolveIfPermanentLosesItsLastTimeCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AetherMembrane());
        target.setCounterCount(CounterType.TIME, 1);
        harness.setHand(player1, List.of(new Timecrafting()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castModalInstantForX(player1, 0, 1, 2, target.getId());

        target.setCounterCount(CounterType.TIME, 0);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.TIME)).isZero();
        harness.assertInGraveyard(player1, "Timecrafting");
    }

    @Test
    void cannotTargetExiledCardAfterItsLastTimeCounterIsGone() {
        AeonChronicler target = suspendedCard(0);

        assertThatThrownBy(() -> cast(0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removingLastSuspendCounterQueuesCastingAndEachCounterRemovalTrigger() {
        AeonChronicler target = suspendedCard(2);

        cast(0, 5, target.getId());

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(target.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
        assertThat(gd.stack).hasSize(3);
        harness.assertNotOnBattlefield(player2, "Aeon Chronicler");
    }

    @Test
    void suspendedCardBecomingUnsuspendedBeforeResolutionCannotReceiveCounters() {
        AeonChronicler target = suspendedCard(1);
        harness.setHand(player1, List.of(new Timecrafting()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castModalInstantForX(player1, 0, 1, 2, target.getId());

        gd.exiledCardTimeCounters.remove(target.getId());
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(target.getId());
        harness.assertInGraveyard(player1, "Timecrafting");
    }

    @Test
    void removesTimeCountersFromCardGrantedSuspendByDelay() {
        AetherMembrane target = delayedCard();

        cast(0, 2, target.getId());

        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(target.getId(), player1.getId(), 1));
    }

    @Test
    void addsTimeCountersToCardGrantedSuspendByDelay() {
        AetherMembrane target = delayedCard();

        cast(1, 2, target.getId());

        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(target.getId(), player1.getId(), 5));
    }

    private AetherMembrane delayedCard() {
        AetherMembrane target = new AetherMembrane();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.setHand(player2, List.of(new Delay()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        return target;
    }

    private AeonChronicler suspendedCard(int timeCounters) {
        AeonChronicler target = new AeonChronicler();
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
