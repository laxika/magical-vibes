package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Firescreamer.class)
class FirescreamerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives Firescreamer +1/+0 until end of turn")
    void activatingAbilityBoosts() {
        Permanent firescreamer = addCreatureReady(player1, new Firescreamer());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(firescreamer.getPowerModifier()).isEqualTo(1);
        assertThat(firescreamer.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Multiple activations stack")
    void multipleActivationsStack() {
        Permanent firescreamer = addCreatureReady(player1, new Firescreamer());
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(firescreamer.getPowerModifier()).isEqualTo(3);
        assertThat(firescreamer.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost wears off at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent firescreamer = addCreatureReady(player1, new Firescreamer());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(firescreamer.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(firescreamer.getPowerModifier()).isEqualTo(0);
        assertThat(firescreamer.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The ability cannot be activated without red mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new Firescreamer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The ability cannot be activated with only non-red mana")
    void cannotActivateWithOnlyNonRedMana() {
        addCreatureReady(player1, new Firescreamer());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
