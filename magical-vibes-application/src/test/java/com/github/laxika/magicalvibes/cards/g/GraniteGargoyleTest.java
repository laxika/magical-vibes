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

@CardUsed({GraniteGargoyle.class})
class GraniteGargoyleTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives Granite Gargoyle +0/+1")
    void resolvingAbilityBoostsToughness() {
        Permanent gargoyle = addCreatureReady(player1, new GraniteGargoyle());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gargoyle.getEffectivePower()).isEqualTo(2);
        assertThat(gargoyle.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can activate multiple times if mana allows")
    void canActivateMultipleTimes() {
        Permanent gargoyle = addCreatureReady(player1, new GraniteGargoyle());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gargoyle.getEffectivePower()).isEqualTo(2);
        assertThat(gargoyle.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Can activate ability even when tapped")
    void canActivateWhenTapped() {
        Permanent gargoyle = addCreatureReady(player1, new GraniteGargoyle());
        gargoyle.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gargoyle.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness")
    void canActivateWithSummoningSickness() {
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player1, new GraniteGargoyle());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gargoyle.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent gargoyle = addCreatureReady(player1, new GraniteGargoyle());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gargoyle.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gargoyle.getPowerModifier()).isEqualTo(0);
        assertThat(gargoyle.getToughnessModifier()).isEqualTo(0);
        assertThat(gargoyle.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new GraniteGargoyle());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

}
