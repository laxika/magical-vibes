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
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, invoker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, invoker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, invoker, Keyword.FLYING)).isFalse();
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

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
