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
        return addCreatureReady(player, new IridescentBlademaster());
    }

    @Test
    @DisplayName("Summoning sickness does not prevent activating the ability")
    void canActivateWhileSummoningSick() {
        Permanent blademaster = harness.addToBattlefieldAndReturn(player1, new IridescentBlademaster());
        blademaster.setSummoningSick(true);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(blademaster.getPowerModifier()).isEqualTo(2);
        assertThat(blademaster.getToughnessModifier()).isEqualTo(2);
        assertThat(blademaster.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Four colorless mana cannot pay the green component")
    void cannotActivateWithoutGreenMana() {
        addReadyBlademaster(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The boost waits for resolution and affects only its source")
    void boostOnlyAppliesToSourceOnResolution() {
        Permanent source = addReadyBlademaster(player1);
        Permanent other = addReadyBlademaster(player1);
        Permanent opposing = addReadyBlademaster(player2);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(source.getPowerModifier()).isEqualTo(0);
        assertThat(source.getToughnessModifier()).isEqualTo(0);

        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(2);
        assertThat(source.getToughnessModifier()).isEqualTo(2);
        assertThat(other.getPowerModifier()).isEqualTo(0);
        assertThat(other.getToughnessModifier()).isEqualTo(0);
        assertThat(opposing.getPowerModifier()).isEqualTo(0);
        assertThat(opposing.getToughnessModifier()).isEqualTo(0);
    }
}
