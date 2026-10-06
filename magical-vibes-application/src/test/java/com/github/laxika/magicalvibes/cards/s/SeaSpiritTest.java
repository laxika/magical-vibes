package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeaSpirit.class})
class SeaSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("{U}: gives +1/+0 until end of turn")
    void firebreathingBoosts() {
        Permanent seaSpirit = addCreatureReady(player1, new SeaSpirit());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve ability

        assertThat(seaSpirit.getPowerModifier()).isEqualTo(1);
        assertThat(seaSpirit.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activating the ability multiple times stacks")
    void firebreathingStacks() {
        Permanent seaSpirit = addCreatureReady(player1, new SeaSpirit());

        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(seaSpirit.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Activation requires blue mana")
    void activationRequiresBlueMana() {
        addCreatureReady(player1, new SeaSpirit());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability can be activated while tapped")
    void abilityCanBeActivatedWhileTapped() {
        Permanent seaSpirit = addCreatureReady(player1, new SeaSpirit());
        seaSpirit.tap();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(seaSpirit.getPowerModifier()).isEqualTo(1);
        assertThat(seaSpirit.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability can be activated while summoning sick")
    void abilityCanBeActivatedWhileSummoningSick() {
        Permanent seaSpirit = harness.addToBattlefieldAndReturn(player1, new SeaSpirit());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(seaSpirit.isSummoningSick()).isTrue();
        assertThat(seaSpirit.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent seaSpirit = addCreatureReady(player1, new SeaSpirit());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(seaSpirit.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(seaSpirit.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activations use the stack and boost only their source")
    void pendingActivationsBoostOnlyTheirSource() {
        Permanent source = addCreatureReady(player1, new SeaSpirit());
        Permanent other = addCreatureReady(player1, new SeaSpirit());
        Permanent opposing = addCreatureReady(player2, new SeaSpirit());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(source.getPowerModifier()).isZero();
        harness.passBothPriorities();
        assertThat(source.getPowerModifier()).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(2);
        assertThat(source.getToughnessModifier()).isZero();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(opposing.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Each activation consumes one blue mana without tapping the creature")
    void activationConsumesBlueMana() {
        Permanent seaSpirit = addCreatureReady(player1, new SeaSpirit());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(seaSpirit.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(seaSpirit.getPowerModifier()).isEqualTo(1);
    }
}
