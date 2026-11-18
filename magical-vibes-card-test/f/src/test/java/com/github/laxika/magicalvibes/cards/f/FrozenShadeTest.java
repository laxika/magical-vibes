package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrozenShade.class})
class FrozenShadeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +1/+1 to Frozen Shade")
    void resolvingAbilityBoosts() {
        Permanent shade = addCreatureReady(player1, new FrozenShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can activate multiple times if mana allows")
    void canActivateMultipleTimes() {
        Permanent shade = addCreatureReady(player1, new FrozenShade());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent shade = addCreatureReady(player1, new FrozenShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, shade)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, shade)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate without haste while summoning sick because the ability does not tap")
    void canActivateWhileSummoningSick() {
        Permanent shade = harness.addToBattlefieldAndReturn(player1, new FrozenShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new FrozenShade());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate ability with only mana of the wrong color")
    void cannotActivateWithWrongColorMana() {
        addCreatureReady(player1, new FrozenShade());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped Frozen Shade can activate its ability")
    void canActivateWhileTapped() {
        Permanent shade = addCreatureReady(player1, new FrozenShade());
        shade.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(2);
        assertThat(shade.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The boost waits for resolution and affects only the activating Shade")
    void boostAppliesOnlyToSourceOnResolution() {
        Permanent first = addCreatureReady(player1, new FrozenShade());
        Permanent source = addCreatureReady(player1, new FrozenShade());
        Permanent opposing = addCreatureReady(player2, new FrozenShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gqs.getEffectivePower(gd, source)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, first)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposing)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(1);
    }
}
