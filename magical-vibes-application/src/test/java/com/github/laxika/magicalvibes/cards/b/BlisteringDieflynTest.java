package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BlisteringDieflyn.class)
class BlisteringDieflynTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +1/+0, payable with red mana")
    void resolvingBoostsWithRed() {
        Permanent dieflyn = addCreatureReady(player1, new BlisteringDieflyn());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dieflyn.getEffectivePower()).isEqualTo(1);
        assertThat(dieflyn.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability is also payable with black mana (hybrid cost)")
    void payableWithBlack() {
        Permanent dieflyn = addCreatureReady(player1, new BlisteringDieflyn());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dieflyn.getEffectivePower()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability cannot be paid with an unrelated color")
    void cannotActivateWithUnrelatedMana() {
        addCreatureReady(player1, new BlisteringDieflyn());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate multiple times, stacking the boost")
    void stacksMultipleActivations() {
        Permanent dieflyn = addCreatureReady(player1, new BlisteringDieflyn());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dieflyn.getEffectivePower()).isEqualTo(2);
        assertThat(dieflyn.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent dieflyn = addCreatureReady(player1, new BlisteringDieflyn());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dieflyn.getEffectivePower()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dieflyn.getEffectivePower()).isEqualTo(0);
        assertThat(dieflyn.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new BlisteringDieflyn());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick without untapping")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent dieflyn = harness.addToBattlefieldAndReturn(player1, new BlisteringDieflyn());
        dieflyn.setSummoningSick(true);
        dieflyn.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dieflyn.getEffectivePower()).isEqualTo(1);
        assertThat(dieflyn.getEffectiveToughness()).isEqualTo(1);
        assertThat(dieflyn.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The boost uses the stack and affects only the source creature")
    void boostsOnlySourceOnResolution() {
        Permanent other = addCreatureReady(player1, new BlisteringDieflyn());
        Permanent source = addCreatureReady(player1, new BlisteringDieflyn());
        Permanent opponent = addCreatureReady(player2, new BlisteringDieflyn());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(source.getEffectivePower()).isZero();
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(1);
        assertThat(source.getEffectiveToughness()).isEqualTo(1);
        assertThat(other.getEffectivePower()).isZero();
        assertThat(opponent.getEffectivePower()).isZero();
    }

    @Test
    @DisplayName("Activations paid with different hybrid colors can resolve together")
    void stacksPendingActivationsWithMixedMana() {
        Permanent dieflyn = addCreatureReady(player1, new BlisteringDieflyn());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(dieflyn.getEffectivePower()).isEqualTo(2);
        assertThat(dieflyn.getEffectiveToughness()).isEqualTo(1);
        assertThat(dieflyn.isTapped()).isFalse();
    }
}
