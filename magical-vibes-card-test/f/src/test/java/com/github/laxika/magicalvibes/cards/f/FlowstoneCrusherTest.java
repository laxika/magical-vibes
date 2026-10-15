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

@CardUsed({FlowstoneCrusher.class})
class FlowstoneCrusherTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability gives +1/-1")
    void activatingAbilityBoosts() {
        Permanent crusher = addCreatureReady(player1, new FlowstoneCrusher());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crusher.getPowerModifier()).isEqualTo(1);
        assertThat(crusher.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Can activate multiple times — each gives +1/-1")
    void canActivateMultipleTimes() {
        Permanent crusher = addCreatureReady(player1, new FlowstoneCrusher());
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(crusher.getPowerModifier()).isEqualTo(3);
        assertThat(crusher.getToughnessModifier()).isEqualTo(-3);
    }

    @Test
    @DisplayName("Ability requires {R} mana to activate")
    void abilityRequiresRedMana() {
        addCreatureReady(player1, new FlowstoneCrusher());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent crusher = addCreatureReady(player1, new FlowstoneCrusher());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crusher.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(crusher.getPowerModifier()).isEqualTo(0);
        assertThat(crusher.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent crusher = harness.addToBattlefieldAndReturn(player1, new FlowstoneCrusher());
        crusher.setSummoningSick(true);
        crusher.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crusher.getPowerModifier()).isEqualTo(1);
        assertThat(crusher.getToughnessModifier()).isEqualTo(-1);
        assertThat(crusher.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability boosts only its source among multiple Crushers")
    void boostsOnlyItsSource() {
        Permanent source = addCreatureReady(player1, new FlowstoneCrusher());
        Permanent other = addCreatureReady(player1, new FlowstoneCrusher());
        Permanent opponent = addCreatureReady(player2, new FlowstoneCrusher());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(1);
        assertThat(source.getToughnessModifier()).isEqualTo(-1);
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(opponent.getPowerModifier()).isZero();
        assertThat(opponent.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Zero toughness kills the source before its remaining activation resolves")
    void zeroToughnessKillsSourceWithActivationStillOnStack() {
        addCreatureReady(player1, new FlowstoneCrusher());
        Permanent other = addCreatureReady(player1, new FlowstoneCrusher());
        harness.addMana(player1, ManaColor.RED, 5);

        for (int i = 0; i < 5; i++) {
            harness.activateAbility(player1, 0, null, null);
        }
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(other);
        harness.assertInGraveyard(player1, "Flowstone Crusher");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
    }
}
