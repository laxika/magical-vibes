package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed(Tyrranax.class)
class TyrranaxTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives Tyrranax -1/+1 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent tyrranax = addReadyTyrranax(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(tyrranax.getPowerModifier()).isEqualTo(-1);
        assertThat(tyrranax.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly")
    void repeatedActivationsStack() {
        Permanent tyrranax = addReadyTyrranax(player1);
        addAbilityMana(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(tyrranax.getPowerModifier()).isEqualTo(-2);
        assertThat(tyrranax.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability can be activated while Tyrranax has summoning sickness")
    void abilityDoesNotRequireTyrranaxToHaveBeenUnderControlSinceTurnStart() {
        Permanent tyrranax = harness.addToBattlefieldAndReturn(player1, new Tyrranax());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(tyrranax.getPowerModifier()).isEqualTo(-1);
        assertThat(tyrranax.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability's effect wears off at end of turn")
    void abilityWearsOffAtEndOfTurn() {
        Permanent tyrranax = addReadyTyrranax(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(tyrranax.getPowerModifier()).isZero();
        assertThat(tyrranax.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The ability cannot be activated without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyTyrranax(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyTyrranax(Player player) {
        return addCreatureReady(player, new Tyrranax());
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }
}
