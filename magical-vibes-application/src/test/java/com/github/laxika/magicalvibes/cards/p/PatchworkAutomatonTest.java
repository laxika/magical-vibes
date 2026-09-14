package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PatchworkAutomaton.class, GrizzlyBears.class, Shock.class})
class PatchworkAutomatonTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an artifact spell puts a +1/+1 counter on Patchwork Automaton")
    void artifactSpellPutsCounterOnSource() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new PatchworkAutomaton());

        harness.setHand(player1, List.of(new PatchworkAutomaton()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(automaton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a nonartifact spell does not put a counter on Patchwork Automaton")
    void nonartifactSpellDoesNotPutCounterOnSource() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new PatchworkAutomaton());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(automaton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent casting an artifact spell does not put a counter on Patchwork Automaton")
    void opponentArtifactSpellDoesNotPutCounterOnSource() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new PatchworkAutomaton());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new PatchworkAutomaton()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(automaton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they do not pay {2}")
    void wardCountersUnpaidSpell() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new PatchworkAutomaton());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, automaton.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
    }
}
