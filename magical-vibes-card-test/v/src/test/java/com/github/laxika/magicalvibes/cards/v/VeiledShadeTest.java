package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VeiledShade.class})
class VeiledShadeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +1/+1 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent shade = addCreatureReady(player1, new VeiledShade());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shade.getPowerModifier()).isEqualTo(1);
        assertThat(shade.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly for a cumulative boost")
    void repeatedActivationsStack() {
        Permanent shade = addCreatureReady(player1, new VeiledShade());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shade.getPowerModifier()).isEqualTo(2);
        assertThat(shade.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability does not require tapping")
    void abilityDoesNotRequireTapping() {
        Permanent shade = addCreatureReady(player1, new VeiledShade());
        shade.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new VeiledShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent shade = addCreatureReady(player1, new VeiledShade());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shade.getPowerModifier()).isEqualTo(0);
        assertThat(shade.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("A summoning-sick Shade can activate its ability")
    void summoningSickShadeCanActivate() {
        Permanent shade = harness.addToBattlefieldAndReturn(player1, new VeiledShade());
        shade.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shade.getPowerModifier()).isEqualTo(1);
        assertThat(shade.getToughnessModifier()).isEqualTo(1);
        assertThat(shade.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Two generic mana cannot pay the required black mana")
    void cannotActivateWithoutBlackMana() {
        addCreatureReady(player1, new VeiledShade());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the activating Shade is boosted, and only on resolution")
    void boostAppliesOnlyToSourceOnResolution() {
        Permanent shade = addCreatureReady(player1, new VeiledShade());
        Permanent otherShade = addCreatureReady(player1, new VeiledShade());
        Permanent opposingShade = addCreatureReady(player2, new VeiledShade());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(shade.getPowerModifier()).isEqualTo(0);
        assertThat(shade.getToughnessModifier()).isEqualTo(0);

        harness.passBothPriorities();

        assertThat(shade.getPowerModifier()).isEqualTo(1);
        assertThat(shade.getToughnessModifier()).isEqualTo(1);
        assertThat(otherShade.getPowerModifier()).isEqualTo(0);
        assertThat(otherShade.getToughnessModifier()).isEqualTo(0);
        assertThat(opposingShade.getPowerModifier()).isEqualTo(0);
        assertThat(opposingShade.getToughnessModifier()).isEqualTo(0);
    }
}
