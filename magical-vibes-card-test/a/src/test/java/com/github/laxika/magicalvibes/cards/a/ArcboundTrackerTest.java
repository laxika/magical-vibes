package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcboundTracker.class, DoomBlade.class, Ornithopter.class})
class ArcboundTrackerTest extends BaseCardTest {

    @Test
    void entersWithTwoPlusOnePlusOneCounters() {
        harness.castFromHand(player1, new ArcboundTracker(), "{2}{R}");
        harness.passBothPriorities();

        Permanent tracker = findPermanent(player1, "Arcbound Tracker");
        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void modularMayPutItsCountersOnTargetArtifactCreatureWhenItDies() {
        Permanent tracker = addCreatureReady(player1, new ArcboundTracker());
        tracker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());

        destroyTracker(tracker);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(ornithopter.getId());

        harness.handlePermanentChosen(player1, ornithopter.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ornithopter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void putsACounterOnItForTheSecondAndEachLaterSpellEachTurn() {
        Permanent tracker = addCreatureReady(player1, new ArcboundTracker());
        tracker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new Ornithopter(), new Ornithopter()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private void destroyTracker(Permanent tracker) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, tracker.getId());
    }

    @Test
    void thirdSpellAlsoAddsACounter() {
        Permanent tracker = harness.enterBattlefieldAndReturn(player1, new ArcboundTracker());
        for (int spell = 1; spell <= 3; spell++) {
            harness.castFromHand(player1, new Ornithopter(), "{0}");
            resolveAllTriggers();
            assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                    .isEqualTo(2 + Math.max(0, spell - 1));
        }
    }

    @Test
    void countsSpellsCastBeforeTrackerEnteredIncludingTrackerItself() {
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new ArcboundTracker(), "{2}{R}");
        harness.passBothPriorities();
        Permanent tracker = findPermanent(player1, "Arcbound Tracker");
        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void opponentsSpellsDoNotAddCounters() {
        Permanent tracker = harness.enterBattlefieldAndReturn(player1, new ArcboundTracker());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        for (int spell = 0; spell < 3; spell++) {
            harness.castFromHand(player2, new Ornithopter(), "{0}");
            harness.passBothPriorities();
        }
        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void modularCanBeDeclinedAfterChoosingAnOpponentsArtifactCreature() {
        Permanent tracker = harness.enterBattlefieldAndReturn(player1, new ArcboundTracker());
        Permanent target = addCreatureReady(player2, new Ornithopter());
        destroyTracker(tracker);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void modularTransfersAllCountersPresentAtDeath() {
        Permanent tracker = harness.enterBattlefieldAndReturn(player1, new ArcboundTracker());
        Permanent target = addCreatureReady(player1, new Ornithopter());
        tracker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        destroyTracker(tracker);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void menaceRejectsOneBlockerAndAllowsTwo() {
        Permanent tracker = addCreatureReady(player1, new ArcboundTracker());
        tracker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent first = addCreatureReady(player2, new Ornithopter());
        Permanent second = addCreatureReady(player2, new Ornithopter());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }
}
