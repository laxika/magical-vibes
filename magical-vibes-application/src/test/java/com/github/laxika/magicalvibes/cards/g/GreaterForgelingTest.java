package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GreaterForgeling.class)
class GreaterForgelingTest extends BaseCardTest {

    @Test
    @DisplayName("Its activation gives it +3/-3 until end of turn")
    void activationBoostsSelf() {
        Permanent forgeling = addReadyForgeling();
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(forgeling.getPowerModifier()).isEqualTo(3);
        assertThat(forgeling.getToughnessModifier()).isEqualTo(-3);
    }

    @Test
    @DisplayName("Multiple activations stack")
    void activationsStack() {
        Permanent forgeling = addReadyForgeling();
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
    @DisplayName("The activation boost wears off at end of turn")
    void activationBoostResetsAtEndOfTurn() {
        Permanent forgeling = addReadyForgeling();
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

    private Permanent addReadyForgeling() {
        Permanent forgeling = new Permanent(new GreaterForgeling());
        forgeling.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(forgeling);
        return forgeling;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
