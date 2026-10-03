package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Augmenting Automaton")
@CardUsed({AugmentingAutomaton.class})
class AugmentingAutomatonTest extends BaseCardTest {

    @Test
    @DisplayName("Activating gives it +1/+1 until end of turn")
    void activationBoostsSelf() {
        Permanent automaton = addReadyAutomaton();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(2);
    }

    @Test
    @DisplayName("Repeated activations stack")
    void repeatedActivationsStack() {
        Permanent automaton = addReadyAutomaton();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyAutomaton();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent automaton = addReadyAutomaton();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Automaton can activate without untapping")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new AugmentingAutomaton());
        automaton.setSummoningSick(true);
        automaton.setTapped(true);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(2);
        assertThat(automaton.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Colorless mana cannot pay the black portion of the cost")
    void cannotActivateWithoutBlackMana() {
        Permanent automaton = addReadyAutomaton();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability boosts only its source, even with other Automatons in play")
    void boostsOnlyItsSource() {
        Permanent other = addReadyAutomaton();
        Permanent source = addReadyAutomaton();
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new AugmentingAutomaton());
        addAbilityMana();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(1);
    }

    private Permanent addReadyAutomaton() {
        return addCreatureReady(player1, new AugmentingAutomaton());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
