package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MachinesmithAutomaton.class, AccordersShield.class, GrizzlyBears.class})
class MachinesmithAutomatonTest extends BaseCardTest {

    @Test
    void anotherArtifactEnteringUnderYourControlPutsCounterOnAutomaton() {
        Permanent automaton = addCreatureReady(player1, new MachinesmithAutomaton());

        harness.castFromHand(player1, new AccordersShield(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(automaton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentArtifactDoesNotTriggerAutomaton() {
        Permanent automaton = addCreatureReady(player1, new MachinesmithAutomaton());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new AccordersShield(), "{0}");
        harness.passBothPriorities();

        assertThat(automaton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void nonArtifactEnteringUnderYourControlDoesNotTriggerAutomaton() {
        Permanent automaton = addCreatureReady(player1, new MachinesmithAutomaton());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(automaton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void enteringItselfDoesNotPutCounterOnAutomaton() {
        harness.castFromHand(player1, new MachinesmithAutomaton(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Machinesmith Automaton")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void secondAutomatonTriggersOnlyTheExistingAutomaton() {
        Permanent first = addCreatureReady(player1, new MachinesmithAutomaton());

        harness.castFromHand(player1, new MachinesmithAutomaton(), "{2}{R}");
        harness.passBothPriorities();

        Permanent second = findPermanents(player1, "Machinesmith Automaton").stream()
                .filter(permanent -> permanent != first)
                .findFirst().orElseThrow();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eachArtifactEnteringAddsAnotherCounter() {
        Permanent automaton = addCreatureReady(player1, new MachinesmithAutomaton());

        harness.castFromHand(player1, new AccordersShield(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castFromHand(player1, new AccordersShield(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(automaton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void counterIncreaseContributesToTrampleDamage() {
        Permanent automaton = addCreatureReady(player1, new MachinesmithAutomaton());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new AccordersShield(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Machinesmith Automaton");
        assertThat(automaton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
