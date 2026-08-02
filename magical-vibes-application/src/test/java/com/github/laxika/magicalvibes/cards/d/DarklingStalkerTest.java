package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DarklingStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("First ability grants a regeneration shield")
    void regenerateAbilityGrantsShield() {
        Permanent stalker = addCreatureReady(player1, new DarklingStalker());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(stalker.getRegenerationShield()).isEqualTo(1);
        assertThat(stalker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Second ability pumps +1/+1 and can be activated repeatedly")
    void pumpAbilityStacks() {
        Permanent stalker = addCreatureReady(player1, new DarklingStalker());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stalker)).isEqualTo(3);
        assertThat(stalker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent stalker = addCreatureReady(player1, new DarklingStalker());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, stalker)).isEqualTo(1);
    }
}
