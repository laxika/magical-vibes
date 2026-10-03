package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.t.TidyConclusion;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AccomplishedAutomaton.class, TidyConclusion.class})
class AccomplishedAutomatonTest extends BaseCardTest {

    @Test
    @DisplayName("Fabricate mode puts a +1/+1 counter on Accomplished Automaton")
    void fabricateCountersMode() {
        castAutomaton(0);

        Permanent automaton = findPermanent(player1, "Accomplished Automaton");
        assertThat(automaton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(8);
    }

    @Test
    @DisplayName("Fabricate mode creates a 1/1 colorless Servo artifact creature token")
    void fabricateServoMode() {
        castAutomaton(1);

        List<Permanent> servos = findPermanents(player1, "Servo");

        assertThat(servos).hasSize(1);
        assertThat(servos).allSatisfy(servo -> {
            assertThat(servo.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(gqs.getEffectivePower(gd, servo)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, servo)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Fabricate choice is offered when the triggered ability resolves")
    void fabricateChoiceIsMadeAtResolution() {
        harness.setHand(player1, List.of(new AccomplishedAutomaton()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent automaton = findPermanent(player1, "Accomplished Automaton");
        assertThat(automaton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Servo")).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(automaton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Servo")).isZero();
    }

    @Test
    @DisplayName("Fabricate creates a Servo if the Automaton leaves before resolution")
    void fabricateCreatesServoWhenSourceIsGone() {
        harness.setHand(player1, List.of(new AccomplishedAutomaton()));
        harness.setHand(player2, List.of(new TidyConclusion()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent automaton = findPermanent(player1, "Accomplished Automaton");
        harness.castAndResolveInstant(player2, 0, automaton.getId());
        assertThat(countPermanents(player1, "Accomplished Automaton")).isZero();

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Servo")).isEqualTo(1);
        assertThat(countPermanents(player2, "Servo")).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castAutomaton(int mode) {
        harness.setHand(player1, List.of(new AccomplishedAutomaton()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, mode == 0);
        resolveAllTriggers();
    }
}
