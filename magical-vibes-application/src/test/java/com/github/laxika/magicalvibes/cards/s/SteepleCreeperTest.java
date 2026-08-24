package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SteepleCreeperTest extends BaseCardTest {

    @Test
    @DisplayName("Activating gives Steeple Creeper flying until end of turn")
    void grantsFlying() {
        Permanent creeper = harness.addToBattlefieldAndReturn(player1, new SteepleCreeper());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creeper, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOff() {
        Permanent creeper = harness.addToBattlefieldAndReturn(player1, new SteepleCreeper());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creeper, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefieldAndReturn(player1, new SteepleCreeper());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
