package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HypnoticGrifter;
import com.github.laxika.magicalvibes.cards.m.MadameMasque;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronMongerSadisticTycoon.class, HypnoticGrifter.class, MadameMasque.class, GrizzlyBears.class, Swamp.class, Murder.class})
class IronMongerSadisticTycoonTest extends BaseCardTest {

    @Test
    void connivingPutsCountersOnEachVillainYouControl() {
        Permanent ironMonger = addCreatureReady(player1, new IronMongerSadisticTycoon());
        Permanent otherVillain = addCreatureReady(player1, new MadameMasque());
        Permanent nonVillain = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentVillain = addCreatureReady(player2, new MadameMasque());
        addCreatureReady(player1, new HypnoticGrifter());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 3, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(ironMonger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherVillain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonVillain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentVillain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void discardingALandStillTriggersCountersForVillains() {
        Permanent ironMonger = addCreatureReady(player1, new IronMongerSadisticTycoon());
        Permanent grifter = addCreatureReady(player1, new HypnoticGrifter());
        harness.setHand(player1, List.of(new Swamp()));
        harness.setLibrary(player1, List.of(new Swamp()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(ironMonger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(grifter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentConnivingDoesNotTriggerCounters() {
        Permanent ironMonger = addCreatureReady(player1, new IronMongerSadisticTycoon());
        Permanent grifter = addCreatureReady(player2, new HypnoticGrifter());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(grifter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ironMonger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void connivingAfterTheCreatureLeavesStillTriggersCounters() {
        Permanent ironMonger = addCreatureReady(player1, new IronMongerSadisticTycoon());
        Permanent grifter = addCreatureReady(player1, new HypnoticGrifter());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, null);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, grifter.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hypnotic Grifter");
        assertThat(ironMonger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void queuedTriggerStillResolvesAfterIronMongerLeaves() {
        Permanent ironMonger = addCreatureReady(player1, new IronMongerSadisticTycoon());
        Permanent villain = addCreatureReady(player1, new MadameMasque());
        addCreatureReady(player1, new HypnoticGrifter());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, ironMonger.getId());
        harness.passBothPriorities();

        assertThat(villain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
