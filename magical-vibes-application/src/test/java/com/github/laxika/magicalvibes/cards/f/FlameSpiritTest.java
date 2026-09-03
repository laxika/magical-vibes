package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(FlameSpirit.class)
class FlameSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("{R}: gives +1/+0 until end of turn")
    void firebreathingBoosts() {
        Permanent flameSpirit = addCreatureReady(player1, new FlameSpirit());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve ability

        assertThat(flameSpirit.getPowerModifier()).isEqualTo(1);
        assertThat(flameSpirit.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activating the ability multiple times stacks")
    void firebreathingStacks() {
        Permanent flameSpirit = addCreatureReady(player1, new FlameSpirit());

        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(flameSpirit.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Activation requires red mana")
    void activationRequiresRedMana() {
        addCreatureReady(player1, new FlameSpirit());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability can be activated while tapped")
    void abilityCanBeActivatedWhileTapped() {
        Permanent flameSpirit = addCreatureReady(player1, new FlameSpirit());
        flameSpirit.tap();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(flameSpirit.getPowerModifier()).isEqualTo(1);
        assertThat(flameSpirit.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability can be activated while summoning sick")
    void abilityCanBeActivatedWhileSummoningSick() {
        Permanent flameSpirit = harness.addToBattlefieldAndReturn(player1, new FlameSpirit());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(flameSpirit.isSummoningSick()).isTrue();
        assertThat(flameSpirit.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent flameSpirit = addCreatureReady(player1, new FlameSpirit());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(flameSpirit.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(flameSpirit.getPowerModifier()).isEqualTo(0);
    }
}
