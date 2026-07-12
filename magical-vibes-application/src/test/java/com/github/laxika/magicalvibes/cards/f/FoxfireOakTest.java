package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FoxfireOakTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability gives +3/+0 (paid with green mana)")
    void activatingAbilityBoostsPowerWithGreen() {
        Permanent oak = addReadyFoxfireOak(player1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(oak.getPowerModifier()).isEqualTo(3);
        assertThat(oak.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability can also be paid with red mana (hybrid)")
    void activatingAbilityBoostsPowerWithRed() {
        Permanent oak = addReadyFoxfireOak(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(oak.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can activate multiple times — each gives +3/+0")
    void canActivateMultipleTimes() {
        Permanent oak = addReadyFoxfireOak(player1);
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(oak.getPowerModifier()).isEqualTo(6);
        assertThat(oak.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent oak = addReadyFoxfireOak(player1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(oak.getPowerModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(oak.getPowerModifier()).isEqualTo(0);
        assertThat(oak.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent addReadyFoxfireOak(Player player) {
        FoxfireOak card = new FoxfireOak();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
