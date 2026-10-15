package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrossRipper.class})
class DrossRipperTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingAbilityPutsOnStack() {
        addCreatureReady(player1, new DrossRipper());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getSourcePermanentId())
                .isEqualTo(gd.playerBattlefields.get(player1.getId()).getFirst().getId());
    }

    @Test
    @DisplayName("Resolving ability gives +1/+1 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent ripper = addCreatureReady(player1, new DrossRipper());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ripper.getPowerModifier()).isEqualTo(1);
        assertThat(ripper.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate multiple times for cumulative boost")
    void canActivateMultipleTimesForCumulativeBoost() {
        Permanent ripper = addCreatureReady(player1, new DrossRipper());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ripper.getPowerModifier()).isEqualTo(2);
        assertThat(ripper.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability does not require tapping")
    void abilityDoesNotRequireTapping() {
        Permanent ripper = addCreatureReady(player1, new DrossRipper());
        ripper.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new DrossRipper());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate while summoning sick")
    void canActivateWhileSummoningSick() {
        Permanent ripper = harness.addToBattlefieldAndReturn(player1, new DrossRipper());
        ripper.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ripper.getPowerModifier()).isEqualTo(1);
        assertThat(ripper.getToughnessModifier()).isEqualTo(1);
        assertThat(ripper.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot replace the black mana with generic mana")
    void cannotActivateWithoutBlackMana() {
        addCreatureReady(player1, new DrossRipper());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the activating Dross Ripper gets the boost")
    void onlyBoostsItsSource() {
        Permanent other = addCreatureReady(player1, new DrossRipper());
        Permanent source = addCreatureReady(player1, new DrossRipper());
        Permanent opponent = addCreatureReady(player2, new DrossRipper());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(1);
        assertThat(source.getToughnessModifier()).isEqualTo(1);
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(opponent.getPowerModifier()).isZero();
        assertThat(opponent.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent ripper = addCreatureReady(player1, new DrossRipper());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ripper.getPowerModifier()).isEqualTo(1);
        assertThat(ripper.getToughnessModifier()).isEqualTo(1);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ripper.getPowerModifier()).isEqualTo(0);
        assertThat(ripper.getToughnessModifier()).isEqualTo(0);
    }

}
