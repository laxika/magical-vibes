package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MinotaurSureshotTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{R} gives +1/+0 until end of turn")
    void payingGivesBoost() {
        Permanent sureshot = addCreatureReady(player1, new MinotaurSureshot());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sureshot.getPowerModifier()).isEqualTo(1);
        assertThat(sureshot.getToughnessModifier()).isEqualTo(0);
        assertThat(sureshot.getEffectivePower()).isEqualTo(3);
        assertThat(sureshot.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Activating twice stacks the boost")
    void activatingTwiceStacks() {
        Permanent sureshot = addCreatureReady(player1, new MinotaurSureshot());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sureshot.getEffectivePower()).isEqualTo(4);
        assertThat(sureshot.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent sureshot = addCreatureReady(player1, new MinotaurSureshot());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(sureshot.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sureshot.getPowerModifier()).isEqualTo(0);
        assertThat(sureshot.getEffectivePower()).isEqualTo(2);
    }
}
