package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DragonHatchlingTest extends BaseCardTest {

    @Test
    @DisplayName("Pump ability grants +1/+0 until end of turn")
    void pumpAbilityGrantsBoost() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hatchling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hatchling)).isEqualTo(1);
    }

    @Test
    @DisplayName("Pump ability can be activated repeatedly in a turn")
    void pumpAbilityStacks() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hatchling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hatchling)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, hatchling)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hatchling)).isEqualTo(0);
    }

    private Permanent addReadyHatchling(Player player) {
        Permanent perm = new Permanent(new DragonHatchling());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
