package com.github.laxika.magicalvibes.cards.i;

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

@CardUsed(IridescentBlademaster.class)
class IridescentBlademasterTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +2/+2 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent blademaster = addReadyBlademaster(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(blademaster.getPowerModifier()).isEqualTo(2);
        assertThat(blademaster.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly for a cumulative boost")
    void repeatedActivationsStack() {
        Permanent blademaster = addReadyBlademaster(player1);
        addAbilityMana(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(blademaster.getPowerModifier()).isEqualTo(4);
        assertThat(blademaster.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Ability does not require tapping")
    void abilityDoesNotRequireTapping() {
        Permanent blademaster = addReadyBlademaster(player1);
        blademaster.tap();
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyBlademaster(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent blademaster = addReadyBlademaster(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blademaster.getPowerModifier()).isEqualTo(0);
        assertThat(blademaster.getToughnessModifier()).isEqualTo(0);
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }

    private Permanent addReadyBlademaster(Player player) {
        Permanent perm = new Permanent(new IridescentBlademaster());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
