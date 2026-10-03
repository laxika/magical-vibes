package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MotherBear;
import com.github.laxika.magicalvibes.cards.u.UniversalAutomaton;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CordialVampire.class, ChildOfNight.class, GrizzlyBears.class,
        MotherBear.class, UniversalAutomaton.class})
class CordialVampireTest extends BaseCardTest {

    @Test
    void anotherCreatureDiesPutsCountersOnEachVampireYouControl() {
        Permanent cordial = addCreatureReady(player1, new CordialVampire());
        Permanent child = addCreatureReady(player1, new ChildOfNight());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        destroy(bears);

        assertThat(cordial.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(child.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void thisCreatureDyingPutsCounterOnRemainingVampiresYouControl() {
        Permanent cordial = addCreatureReady(player1, new CordialVampire());
        Permanent child = addCreatureReady(player1, new ChildOfNight());

        destroy(cordial);

        assertThat(child.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Cordial Vampire");
    }

    @Test
    void countersOnlyGoToControlledVampiresIncludingChangelings() {
        Permanent cordial = addCreatureReady(player1, new CordialVampire());
        Permanent automaton = addCreatureReady(player1, new UniversalAutomaton());
        Permanent ownBear = addCreatureReady(player1, new MotherBear());
        Permanent opposingVampire = addCreatureReady(player2, new UniversalAutomaton());
        Permanent dyingBear = addCreatureReady(player2, new MotherBear());

        destroy(dyingBear);

        assertThat(cordial.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(automaton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingVampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void simultaneousDeathsIncludingSourceEachPutCountersOnSurvivingVampires() {
        Permanent cordial = addCreatureReady(player1, new CordialVampire());
        Permanent survivor = addCreatureReady(player1, new UniversalAutomaton());
        Permanent bear = addCreatureReady(player2, new MotherBear());
        cordial.setMarkedDamage(1);
        bear.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Cordial Vampire");
        harness.assertInGraveyard(player2, "Mother Bear");
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void lethallyDamagedVampireCannotBeSavedByAnotherCreaturesDeath() {
        Permanent cordial = addCreatureReady(player1, new CordialVampire());
        Permanent automaton = addCreatureReady(player1, new UniversalAutomaton());
        Permanent bear = addCreatureReady(player2, new MotherBear());
        automaton.setMarkedDamage(1);
        bear.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Universal Automaton");
        harness.assertInGraveyard(player2, "Mother Bear");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(cordial.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Universal Automaton");
    }

    @Test
    void vampireEnteringBeforeDeathTriggerResolvesReceivesCounter() {
        Permanent cordial = addCreatureReady(player1, new CordialVampire());
        Permanent bear = addCreatureReady(player1, new MotherBear());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, bear));
        assertThat(cordial.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new UniversalAutomaton());
        harness.passBothPriorities();

        assertThat(cordial.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(newcomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void eachCordialVampireTriggersIndependentlyForOneDeath() {
        Permanent first = addCreatureReady(player1, new CordialVampire());
        Permanent second = addCreatureReady(player1, new CordialVampire());
        Permanent bear = addCreatureReady(player2, new MotherBear());

        destroy(bear);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void destroy(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, permanent));
        harness.passBothPriorities();
    }
}
