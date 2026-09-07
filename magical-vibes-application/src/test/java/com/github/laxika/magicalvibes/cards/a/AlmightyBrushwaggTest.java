package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(AlmightyBrushwagg.class)
class AlmightyBrushwaggTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {3}{G} gives Almighty Brushwagg +3/+3 until end of turn")
    void activatedAbilityBoostsSelf() {
        Permanent brushwagg = addReadyBrushwagg();
        addMana(player1, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, brushwagg)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, brushwagg)).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly")
    void repeatedActivationsStack() {
        Permanent brushwagg = addReadyBrushwagg();
        addMana(player1, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, brushwagg)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, brushwagg)).isEqualTo(7);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent brushwagg = addReadyBrushwagg();
        addMana(player1, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, brushwagg)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, brushwagg)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability cannot be activated without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyBrushwagg();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyBrushwagg() {
        return addCreatureReady(player1, new AlmightyBrushwagg());
    }

    private void addMana(Player player, int amount) {
        harness.addMana(player, ManaColor.GREEN, amount);
        harness.addMana(player, ManaColor.COLORLESS, amount * 3);
    }
}
