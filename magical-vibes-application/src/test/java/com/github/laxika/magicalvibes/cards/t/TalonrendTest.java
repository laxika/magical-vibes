package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TalonrendTest extends BaseCardTest {

    @Test
    @DisplayName("{U/R} paid with blue: gets +1/-1 until end of turn")
    void pumpPaidWithBlue() {
        Permanent talonrend = addCreatureReady(player1, new Talonrend());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(talonrend.getPowerModifier()).isEqualTo(1);
        assertThat(talonrend.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("{U/R} paid with red: hybrid cost accepts either color")
    void pumpPaidWithRed() {
        Permanent talonrend = addCreatureReady(player1, new Talonrend());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(talonrend.getPowerModifier()).isEqualTo(1);
        assertThat(talonrend.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Ability stacks when activated repeatedly")
    void pumpStacks() {
        Permanent talonrend = addCreatureReady(player1, new Talonrend());
        harness.addMana(player1, ManaColor.BLUE, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(talonrend.getPowerModifier()).isEqualTo(3);
        assertThat(talonrend.getToughnessModifier()).isEqualTo(-3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent talonrend = addCreatureReady(player1, new Talonrend());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(talonrend.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(talonrend.getPowerModifier()).isEqualTo(0);
        assertThat(talonrend.getToughnessModifier()).isEqualTo(0);
    }
}
