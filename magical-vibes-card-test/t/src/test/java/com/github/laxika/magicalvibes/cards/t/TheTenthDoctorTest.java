package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheTenthDoctor.class, Forest.class, SolRing.class, TimeBeetle.class})
class TheTenthDoctorTest extends BaseCardTest {

    @Test
    void attackingExilesUntilNonlandAndSuspendsTheFoundCard() {
        addCreatureReady(player1, new TheTenthDoctor());
        Forest land = new Forest();
        SolRing nonland = new SolRing();
        harness.setLibrary(player1, List.of(land, nonland));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card)
                .containsExactly(land, nonland);
        assertThat(gd.exiledCardTimeCounters).containsEntry(nonland.getId(), 3);
    }

    @Test
    void timeTravelCanAddCountersAcrossAllThreeEvents() {
        harness.addToBattlefield(player1, new TheTenthDoctor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SolRing());
        target.setCounterCount(CounterType.TIME, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ADD");
        harness.handleListChoice(player1, "SKIP");
        harness.handleListChoice(player1, "SKIP");

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    void timeTravelCanRemoveTheLastCounterFromAControlledPermanent() {
        harness.addToBattlefield(player1, new TheTenthDoctor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SolRing());
        target.setCounterCount(CounterType.TIME, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "REMOVE");

        assertThat(target.getCounterCount(CounterType.TIME)).isZero();
    }

    @Test
    void attackingWithAnotherCreatureTriggersWhileTheDoctorStaysBack() {
        harness.addToBattlefield(player1, new TheTenthDoctor());
        addCreatureReady(player1, new TimeBeetle());
        SolRing nonland = new SolRing();
        harness.setLibrary(player1, List.of(nonland));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            resolveAllTriggers();
        });

        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card).containsExactly(nonland);
        assertThat(gd.exiledCardTimeCounters).containsEntry(nonland.getId(), 3);
    }

    @Test
    void attackingWithMultipleCreaturesTriggersOnlyOnce() {
        addCreatureReady(player1, new TheTenthDoctor());
        addCreatureReady(player1, new TimeBeetle());
        SolRing first = new SolRing();
        SolRing second = new SolRing();
        harness.setLibrary(player1, List.of(first, second));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            resolveAllTriggers();
        });

        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    void allLandLibraryIsExiledWithoutSuspendingALand() {
        addCreatureReady(player1, new TheTenthDoctor());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card).containsExactly(first, second);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKeys(first.getId(), second.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void timeTravelCanAddACounterOnEachOfTheThreeEvents() {
        harness.addToBattlefield(player1, new TheTenthDoctor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SolRing());
        target.setCounterCount(CounterType.TIME, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ADD");
        harness.handleListChoice(player1, "ADD");
        harness.handleListChoice(player1, "ADD");

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(4);
    }

    @Test
    void timeTravelDoesNotAffectOpponentsPermanentsOrPermanentsWithoutTimeCounters() {
        harness.addToBattlefield(player1, new TheTenthDoctor());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new SolRing());
        opponent.setCounterCount(CounterType.TIME, 2);
        Permanent own = harness.addToBattlefieldAndReturn(player1, new SolRing());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(opponent.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(own.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void timeTravelCanRemoveAllThreeCountersFromTheCardSuspendedByAllonsY() {
        addCreatureReady(player1, new TheTenthDoctor());
        SolRing suspended = new SolRing();
        harness.setLibrary(player1, List.of(suspended));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "REMOVE");
        harness.handleListChoice(player1, "REMOVE");
        harness.handleListChoice(player1, "REMOVE");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Sol Ring");
        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card).doesNotContain(suspended);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(suspended.getId());
    }

    @Test
    void timeTravelCanAddCountersToTheCardSuspendedByAllonsY() {
        addCreatureReady(player1, new TheTenthDoctor());
        SolRing suspended = new SolRing();
        harness.setLibrary(player1, List.of(suspended));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ADD");
        harness.handleListChoice(player1, "ADD");
        harness.handleListChoice(player1, "ADD");

        assertThat(gd.exiledCardTimeCounters).containsEntry(suspended.getId(), 6);
        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card).contains(suspended);
    }

    @Test
    void timeTravelChangesCountersSimultaneouslyAfterAllChoicesForOneEvent() {
        harness.addToBattlefield(player1, new TheTenthDoctor());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SolRing());
        first.setCounterCount(CounterType.TIME, 1);
        second.setCounterCount(CounterType.TIME, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "REMOVE");

        assertThat(first.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.TIME)).isEqualTo(1);

        harness.handleListChoice(player1, "REMOVE");
        assertThat(first.getCounterCount(CounterType.TIME)).isZero();
        assertThat(second.getCounterCount(CounterType.TIME)).isZero();
    }

    @Test
    void timeTravelCannotBeActivatedDuringCombat() {
        harness.addToBattlefield(player1, new TheTenthDoctor());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
