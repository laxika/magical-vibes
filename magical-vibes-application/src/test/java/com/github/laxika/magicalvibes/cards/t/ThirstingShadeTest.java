package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThirstingShadeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +1/+1 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent shade = addReadyShade(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shade.getPowerModifier()).isEqualTo(1);
        assertThat(shade.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly for a cumulative boost")
    void repeatedActivationsStack() {
        Permanent shade = addReadyShade(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shade.getPowerModifier()).isEqualTo(2);
        assertThat(shade.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyShade(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent shade = addReadyShade(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shade.getPowerModifier()).isEqualTo(0);
        assertThat(shade.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent addReadyShade(Player player) {
        Permanent perm = new Permanent(new ThirstingShade());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
