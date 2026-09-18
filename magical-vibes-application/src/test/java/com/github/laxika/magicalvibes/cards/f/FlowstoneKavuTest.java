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

@CardUsed(FlowstoneKavu.class)
class FlowstoneKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {R} gives Flowstone Kavu +1/-1 until end of turn")
    void activatedAbilityBoostsSelf() {
        Permanent kavu = addCreatureReady(player1, new FlowstoneKavu());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kavu.getEffectivePower()).isEqualTo(3);
        assertThat(kavu.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly")
    void repeatedActivationsStack() {
        Permanent kavu = addCreatureReady(player1, new FlowstoneKavu());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kavu.getEffectivePower()).isEqualTo(4);
        assertThat(kavu.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent kavu = addCreatureReady(player1, new FlowstoneKavu());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(kavu.getEffectivePower()).isEqualTo(2);
        assertThat(kavu.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability does not require tapping")
    void abilityDoesNotRequireTapping() {
        Permanent kavu = addCreatureReady(player1, new FlowstoneKavu());
        kavu.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(kavu.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability requires red mana")
    void cannotActivateWithoutRedMana() {
        addCreatureReady(player1, new FlowstoneKavu());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
