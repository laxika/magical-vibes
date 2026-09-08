package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LavafumeInvokerTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +3/+0 until end of turn")
    void boostsOwnCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent invoker = addCreatureReady(player1, new LavafumeInvoker());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        activate();

        assertThat(invoker.getEffectivePower()).isEqualTo(5);
        assertThat(invoker.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.getEffectivePower()).isEqualTo(5);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(opponentBears.getEffectivePower()).isEqualTo(2);
        assertThat(opponentBears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new LavafumeInvoker());

        activate();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating the ability requires eight mana")
    void requiresEightMana() {
        addCreatureReady(player1, new LavafumeInvoker());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private void activate() {
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
    }
}
