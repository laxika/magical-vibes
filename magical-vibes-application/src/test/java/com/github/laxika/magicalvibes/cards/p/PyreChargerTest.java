package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PyreChargerTest extends BaseCardTest {

    @Test
    @DisplayName("{R}: gets +1/+0 until end of turn")
    void pumpGivesPlusOnePlusZero() {
        Permanent charger = addCreatureReady(player1, new PyreCharger());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(charger.getPowerModifier()).isEqualTo(1);
        assertThat(charger.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly, stacking the boost")
    void pumpStacks() {
        Permanent charger = addCreatureReady(player1, new PyreCharger());
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(charger.getPowerModifier()).isEqualTo(3);
        assertThat(charger.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent charger = addCreatureReady(player1, new PyreCharger());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(charger.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(charger.getPowerModifier()).isEqualTo(0);
    }
}
