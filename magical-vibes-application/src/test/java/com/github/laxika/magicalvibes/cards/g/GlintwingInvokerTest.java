package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(GlintwingInvoker.class)
class GlintwingInvokerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating gives +3/+3 and flying until end of turn")
    void activatesWithBoostAndFlying() {
        Permanent invoker = harness.addToBattlefieldAndReturn(player1, new GlintwingInvoker());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, invoker)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, invoker)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, invoker, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The boost and flying wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent invoker = harness.addToBattlefieldAndReturn(player1, new GlintwingInvoker());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, invoker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, invoker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, invoker, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Repeated activations stack the boost and keep flying until end of turn")
    void repeatedActivationsStack() {
        Permanent invoker = harness.addToBattlefieldAndReturn(player1, new GlintwingInvoker());
        addActivationMana();
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, invoker)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, invoker)).isEqualTo(9);
        assertThat(gqs.hasKeyword(gd, invoker, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without seven generic and one blue mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new GlintwingInvoker());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped Invoker can activate without becoming untapped")
    void canActivateWhileTapped() {
        Permanent invoker = harness.addToBattlefieldAndReturn(player1, new GlintwingInvoker());
        invoker.setTapped(true);
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(invoker.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, invoker)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, invoker)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, invoker, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The ability affects only the Invoker that activated it")
    void affectsOnlyItsSource() {
        Permanent invoker = harness.addToBattlefieldAndReturn(player1, new GlintwingInvoker());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GlintwingInvoker());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GlintwingInvoker());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, invoker)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, invoker)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, invoker, Keyword.FLYING)).isTrue();
        for (Permanent other : new Permanent[]{ally, opponent}) {
            assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        }
    }

    @Test
    @DisplayName("One blue and six generic mana cannot pay the activation cost")
    void cannotActivateWithTooLittleGenericMana() {
        harness.addToBattlefield(player1, new GlintwingInvoker());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
