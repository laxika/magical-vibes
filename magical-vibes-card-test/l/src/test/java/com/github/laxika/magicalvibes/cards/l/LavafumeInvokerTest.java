package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LavafumeInvoker.class})
class LavafumeInvokerTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +3/+0 until end of turn")
    void boostsOwnCreatures() {
        Permanent bears = addCreatureReady(player1, new LavafumeInvoker());
        Permanent invoker = addCreatureReady(player1, new LavafumeInvoker());
        Permanent opponentBears = addCreatureReady(player2, new LavafumeInvoker());

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
        Permanent bears = addCreatureReady(player1, new LavafumeInvoker());
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

    @Test
    @DisplayName("Multiple activations stack without tapping the invoker")
    void multipleActivationsStack() {
        Permanent invoker = addCreatureReady(player1, new LavafumeInvoker());
        harness.addMana(player1, ManaColor.COLORLESS, 16);
        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(invoker.getEffectivePower()).isEqualTo(8);
        assertThat(invoker.getEffectiveToughness()).isEqualTo(2);
        assertThat(invoker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped summoning-sick invoker can activate on the opponent's turn")
    void activatesWhileTappedAndSummoningSickOnOpponentsTurn() {
        Permanent invoker = harness.addToBattlefieldAndReturn(player1, new LavafumeInvoker());
        invoker.setSummoningSick(true);
        invoker.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(invoker.getEffectivePower()).isEqualTo(5);
        assertThat(invoker.getEffectiveToughness()).isEqualTo(2);
        assertThat(invoker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The boost affects creatures present at resolution, not later arrivals")
    void locksInCreaturesAtResolution() {
        Permanent invoker = addCreatureReady(player1, new LavafumeInvoker());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = harness.enterBattlefieldAndReturn(player1, new LavafumeInvoker());
        harness.passBothPriorities();
        Permanent afterResolution = harness.enterBattlefieldAndReturn(player1, new LavafumeInvoker());

        assertThat(invoker.getEffectivePower()).isEqualTo(5);
        assertThat(beforeResolution.getEffectivePower()).isEqualTo(5);
        assertThat(afterResolution.getEffectivePower()).isEqualTo(2);
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
