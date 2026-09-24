package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CloudspireSkycycle;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HangarbackAssembler.class, HangarbackWalker.class, CloudspireSkycycle.class, GrizzlyBears.class})
class HangarbackAssemblerTest extends BaseCardTest {

    @Test
    void conjuresHangarbackWalkerAsARealPermanentWithACounter() {
        harness.setHand(player1, List.of(new HangarbackAssembler()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
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

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
