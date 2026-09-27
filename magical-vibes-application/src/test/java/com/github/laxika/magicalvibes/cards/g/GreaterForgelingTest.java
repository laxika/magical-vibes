package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(GreaterForgeling.class)
class GreaterForgelingTest extends BaseCardTest {

    @Test
    @DisplayName("Its activation gives it +3/-3 until end of turn")
    void activationBoostsSelf() {
        Permanent forgeling = addCreatureReady(player1, new GreaterForgeling());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(forgeling.getPowerModifier()).isEqualTo(3);
        assertThat(forgeling.getToughnessModifier()).isEqualTo(-3);
    }

    @Test
    @DisplayName("Multiple activations stack")
    void activationsStack() {
        Permanent forgeling = addCreatureReady(player1, new GreaterForgeling());
        addActivationMana();
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(forgeling.getPowerModifier()).isEqualTo(6);
        assertThat(forgeling.getToughnessModifier()).isEqualTo(-6);
    }

    @Test
    @DisplayName("Its activation does not require it to be untapped")
    void activationDoesNotRequireUntappedCreature() {
        Permanent forgeling = addCreatureReady(player1, new GreaterForgeling());
        forgeling.tap();
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(forgeling.getPowerModifier()).isEqualTo(3);
        assertThat(forgeling.getToughnessModifier()).isEqualTo(-3);
    }

    @Test
    @DisplayName("Its activation requires the generic and red mana in its cost")
    void activationRequiresBothManaComponents() {
        addCreatureReady(player1, new GreaterForgeling());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The activation boost wears off at end of turn")
    void activationBoostResetsAtEndOfTurn() {
        Permanent forgeling = addCreatureReady(player1, new GreaterForgeling());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(forgeling.getPowerModifier()).isEqualTo(3);
        assertThat(forgeling.getToughnessModifier()).isEqualTo(-3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(forgeling.getPowerModifier()).isZero();
        assertThat(forgeling.getToughnessModifier()).isZero();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
