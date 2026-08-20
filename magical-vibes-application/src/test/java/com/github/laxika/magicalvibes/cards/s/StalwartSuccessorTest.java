package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GavonyTownship;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

    private void activateTownship(Permanent township) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(township), 1, null, null);
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
