package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FireBellyChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("{R}: gets +1/+0 until end of turn")
    void pumpGivesPlusOnePlusZero() {
        Permanent changeling = addCreatureReady(player1, new FireBellyChangeling());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(changeling.getPowerModifier()).isEqualTo(1);
        assertThat(changeling.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can be activated twice, stacking to +2/+0")
    void pumpStacksTwice() {
        Permanent changeling = addCreatureReady(player1, new FireBellyChangeling());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(changeling.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot be activated more than twice each turn")
    void cannotActivateMoreThanTwice() {
        addCreatureReady(player1, new FireBellyChangeling());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 2 times");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent changeling = addCreatureReady(player1, new FireBellyChangeling());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(changeling.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(changeling.getPowerModifier()).isEqualTo(0);
    }
}
