package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CloudspireSkycycle;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HangarbackAssembler.class, HangarbackWalker.class, CloudspireSkycycle.class, GrizzlyBears.class})
class HangarbackAssemblerTest extends BaseCardTest {

    @Test
    void conjuresHangarbackWalkerAsARealPermanentWithACounter() {
        harness.castFromHand(player1, new HangarbackAssembler(), "{1}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent walker = findPermanent(player1, "Hangarback Walker");
        assertThat(walker.getCard().isToken()).isFalse();
        assertThat(walker.getCard().getOwnerId()).isEqualTo(player1.getId());
        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void maxSpeedCountersArtifactCreaturesAndVehiclesOnly() {
        gd.playerSpeeds.put(player1.getId(), 4);
        Permanent assembler = addCreatureReady(player1, new HangarbackAssembler());
        Permanent walker = addCreatureReady(player1, new HangarbackWalker());
        walker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent vehicle = addCreatureReady(player1, new CloudspireSkycycle());
        Permanent nonArtifactCreature = addCreatureReady(player1, new GrizzlyBears());

        advanceToEndStep(player1);
        resolveAllTriggers();

        assertThat(assembler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonArtifactCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void maxSpeedTriggerDoesNothingBelowMaxSpeed() {
        gd.playerSpeeds.put(player1.getId(), 3);
        Permanent walker = addCreatureReady(player1, new HangarbackWalker());
        walker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player1, new HangarbackAssembler());

        advanceToEndStep(player1);
        resolveAllTriggers();

        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void enteringStartsEnginesAndKeepsTheConjuredWalkerAlive() {
        harness.castFromHand(player1, new HangarbackAssembler(), "{1}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        harness.runStateBasedActions();
        assertThat(countPermanents(player1, "Hangarback Walker")).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Hangarback Walker");
    }

    @Test
    void maxSpeedDoesNotCounterOpposingPermanents() {
        gd.playerSpeeds.put(player1.getId(), 4);
        addCreatureReady(player1, new HangarbackAssembler());
        Permanent ownWalker = addCreatureReady(player1, new HangarbackWalker());
        ownWalker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent opposingWalker = addCreatureReady(player2, new HangarbackWalker());
        opposingWalker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent opposingVehicle = addCreatureReady(player2, new CloudspireSkycycle());

        advanceToEndStep(player1);
        resolveAllTriggers();

        assertThat(ownWalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opposingWalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingVehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void maxSpeedDoesNotTriggerOnOpponentsEndStep() {
        gd.playerSpeeds.put(player1.getId(), 4);
        addCreatureReady(player1, new HangarbackAssembler());
        Permanent walker = addCreatureReady(player1, new HangarbackWalker());
        walker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToEndStep(player2);
        resolveAllTriggers();

        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
