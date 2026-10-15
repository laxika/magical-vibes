package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GavonyTownship;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StalwartSuccessor.class, GavonyTownship.class, GrizzlyBears.class})
class StalwartSuccessorTest extends BaseCardTest {

    @Test
    void putsACounterOnEachCreatureTheFirstTimeItGetsCountersEachTurn() {
        Permanent successor = addCreatureReady(player1, new StalwartSuccessor());
        Permanent township = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());

        activateTownship(township);

        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(successor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerAgainForTheSameCreatureDuringTheTurn() {
        Permanent firstTownship = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        Permanent secondTownship = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new StalwartSuccessor());

        activateTownship(firstTownship);
        activateTownship(secondTownship);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void doesNotTriggerIfTheCreatureReceivedCountersBeforeSuccessorEntered() {
        Permanent firstTownship = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        Permanent secondTownship = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        activateTownship(firstTownship);
        Permanent successor = addCreatureReady(player1, new StalwartSuccessor());
        activateTownship(secondTownship);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(successor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void bothSuccessorsTriggerOnTheSameFirstCounterPlacement() {
        Permanent township = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        Permanent firstSuccessor = addCreatureReady(player1, new StalwartSuccessor());
        Permanent secondSuccessor = addCreatureReady(player1, new StalwartSuccessor());

        activateTownship(township);

        assertThat(firstSuccessor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(secondSuccessor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void doesNotPutCountersOnOpponentsCreatures() {
        Permanent township = harness.addToBattlefieldAndReturn(player2, new GavonyTownship());
        Permanent successor = addCreatureReady(player1, new StalwartSuccessor());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        activateTownship(player2, township);

        assertThat(successor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void activateTownship(Permanent township) {
        activateTownship(player1, township);
    }

    private void activateTownship(Player player, Permanent township) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.activateAbility(player,
                gd.playerBattlefields.get(player.getId()).indexOf(township), 1, null, null);
        resolveAllTriggers();
    }
}
