package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CarrionAntsTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} gives +1/+1 until end of turn")
    void payManaBoostsSelf() {
        Permanent ants = addCreatureReady(player1, new CarrionAnts());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ants)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ants)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability can be activated multiple times to stack the boost")
    void stacksBoost() {
        Permanent ants = addCreatureReady(player1, new CarrionAnts());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ants)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ants)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent ants = addCreatureReady(player1, new CarrionAnts());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ants)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ants)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, ants)).isEqualTo(1);
    }
}
