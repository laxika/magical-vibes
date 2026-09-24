package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcboundTracker.class, DoomBlade.class, Ornithopter.class})
class ArcboundTrackerTest extends BaseCardTest {

    @Test
    void entersWithTwoPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new ArcboundTracker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
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

        gs.playCard(gd, player2, 0, 0, tracker.getId(), null);
        harness.passBothPriorities();
    }
}
