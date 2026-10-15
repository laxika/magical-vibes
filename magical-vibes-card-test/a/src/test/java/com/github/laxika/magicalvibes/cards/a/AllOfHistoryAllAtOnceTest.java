package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ClockworkDroid;
import com.github.laxika.magicalvibes.cards.r.RoryWilliams;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AllOfHistoryAllAtOnce.class, ClockworkDroid.class, RoryWilliams.class})
class AllOfHistoryAllAtOnceTest extends BaseCardTest {

    @Test
    void timeTravelsOnce() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());
        target.setCounterCount(CounterType.TIME, 1);
        castAllOfHistoryAllAtOnce();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ADD");

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    void stormCopiesForEachSpellCastBeforeItThisTurn() {
        gd.recordSpellCast(player1.getId(), new ClockworkDroid());
        castAllOfHistoryAllAtOnce();

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
    }

    @Test
    void canRemoveSkipAndAddIndependentlyWithoutAffectingIneligiblePermanents() {
        Permanent removed = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());
        Permanent skipped = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());
        Permanent added = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());
        Permanent withoutTime = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());
        removed.setCounterCount(CounterType.TIME, 1);
        skipped.setCounterCount(CounterType.TIME, 2);
        added.setCounterCount(CounterType.TIME, 1);
        opponent.setCounterCount(CounterType.TIME, 1);
        castAllOfHistoryAllAtOnce();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "REMOVE");
        harness.handleListChoice(player1, "SKIP");
        harness.handleListChoice(player1, "ADD");

        assertThat(removed.getCounterCount(CounterType.TIME)).isZero();
        assertThat(skipped.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(added.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(withoutTime.getCounterCount(CounterType.TIME)).isZero();
        assertThat(opponent.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void stormCountsBothPlayersSpellsAndEachCopyTimeTravelsSeparately() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());
        target.setCounterCount(CounterType.TIME, 2);
        gd.recordSpellCast(player1.getId(), new ClockworkDroid());
        gd.recordSpellCast(player2.getId(), new ClockworkDroid());
        castAllOfHistoryAllAtOnce();

        harness.passBothPriorities();
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "REMOVE");
        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ADD");
        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(2);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "SKIP");

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getTotalSpellsCastThisTurnCount()).isEqualTo(3);
    }

    @Test
    void resolvesWithoutChoicesOrCopiesWhenNoObjectsAreEligibleAndNoPriorSpellsWereCast() {
        castAllOfHistoryAllAtOnce();
        harness.passBothPriorities();
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "All of History, All at Once");
    }

    @Test
    void timeTravelsRorySuspendedByItsCastTrigger() {
        RoryWilliams rory = new RoryWilliams();
        harness.setHand(player1, List.of(rory));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.suspendedSpellExiles).contains(
                new GameData.SuspendedSpellExile(rory.getId(), player1.getId(), 3));
        castAllOfHistoryAllAtOnce();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ADD");
        assertThat(gd.suspendedSpellExiles).contains(
                new GameData.SuspendedSpellExile(rory.getId(), player1.getId(), 4));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "REMOVE");

        assertThat(gd.suspendedSpellExiles).contains(
                new GameData.SuspendedSpellExile(rory.getId(), player1.getId(), 3));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTimeTravelExiledCardsWithTimeCountersThatDoNotHaveSuspend() {
        ClockworkDroid exiled = new ClockworkDroid();
        harness.setExile(player1, List.of(exiled));
        gd.exiledCardTimeCounters.put(exiled.getId(), 2);
        gd.exiledCardsWithNonSuspendTimeCounters.add(exiled.getId());
        castAllOfHistoryAllAtOnce();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCardTimeCounters.get(exiled.getId())).isEqualTo(2);
        assertThat(gd.exiledCardsWithNonSuspendTimeCounters).contains(exiled.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void timeTravelsOwnedSuspendedCardsButNotOpponentsSuspendedCards() {
        ClockworkDroid owned = new ClockworkDroid();
        ClockworkDroid opponent = new ClockworkDroid();
        harness.setExile(player1, List.of(owned));
        harness.setExile(player2, List.of(opponent));
        gd.exiledCardTimeCounters.put(owned.getId(), 2);
        gd.exiledCardTimeCounters.put(opponent.getId(), 2);
        castAllOfHistoryAllAtOnce();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "REMOVE");

        assertThat(gd.exiledCardTimeCounters.get(owned.getId())).isEqualTo(1);
        assertThat(gd.exiledCardTimeCounters.get(opponent.getId())).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void originalCannotAddCountersToPermanentWhoseLastCounterWasRemovedByCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());
        target.setCounterCount(CounterType.TIME, 1);
        gd.recordSpellCast(player1.getId(), new ClockworkDroid());
        castAllOfHistoryAllAtOnce();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "REMOVE");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void castAllOfHistoryAllAtOnce() {
        harness.castFromHand(player1, new AllOfHistoryAllAtOnce(), "{2}{U}{U}");
    }
}
