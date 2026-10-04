package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GravelHideGoblin.class})
class GravelHideGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +2/+2 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent goblin = addCreatureReady(player1, new GravelHideGoblin());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(goblin.getPowerModifier()).isEqualTo(2);
        assertThat(goblin.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly for a cumulative boost")
    void repeatedActivationsStack() {
        Permanent goblin = addCreatureReady(player1, new GravelHideGoblin());
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(goblin.getPowerModifier()).isEqualTo(4);
        assertThat(goblin.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Ability does not require tapping")
    void abilityDoesNotRequireTapping() {
        Permanent goblin = addCreatureReady(player1, new GravelHideGoblin());
        goblin.tap();
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new GravelHideGoblin());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent goblin = addCreatureReady(player1, new GravelHideGoblin());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(goblin.getPowerModifier()).isEqualTo(0);
        assertThat(goblin.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Four mana without green cannot pay the activation cost")
    void activationRequiresGreenMana() {
        addCreatureReady(player1, new GravelHideGoblin());
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick Goblin can activate its ability")
    void canActivateWhileSummoningSick() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GravelHideGoblin());
        goblin.setSummoningSick(true);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(goblin.getPowerModifier()).isEqualTo(2);
        assertThat(goblin.getToughnessModifier()).isEqualTo(2);
        assertThat(goblin.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only the Goblin whose ability was activated gets the boost")
    void boostsOnlyItsSource() {
        Permanent source = addCreatureReady(player1, new GravelHideGoblin());
        Permanent other = addCreatureReady(player1, new GravelHideGoblin());
        Permanent opposing = addCreatureReady(player2, new GravelHideGoblin());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(2);
        assertThat(source.getToughnessModifier()).isEqualTo(2);
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(opposing.getPowerModifier()).isZero();
        assertThat(opposing.getToughnessModifier()).isZero();
    }
}
