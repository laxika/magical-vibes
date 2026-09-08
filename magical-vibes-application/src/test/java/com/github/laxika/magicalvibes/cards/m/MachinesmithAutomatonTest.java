package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MachinesmithAutomaton.class, AccordersShield.class, GrizzlyBears.class})
class MachinesmithAutomatonTest extends BaseCardTest {

    @Test
    void anotherArtifactEnteringUnderYourControlPutsCounterOnAutomaton() {
        Permanent automaton = addCreatureReady(player1, new MachinesmithAutomaton());

        harness.setHand(player1, List.of(new AccordersShield()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(automaton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentArtifactDoesNotTriggerAutomaton() {
        Permanent automaton = addCreatureReady(player1, new MachinesmithAutomaton());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new AccordersShield()));
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(automaton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void nonArtifactEnteringUnderYourControlDoesNotTriggerAutomaton() {
        Permanent automaton = addCreatureReady(player1, new MachinesmithAutomaton());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(automaton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
