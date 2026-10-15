package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrokersInitiate.class})
class BrokersInitiateTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a 5/5 when the ability is activated with green mana")
    void becomesFiveFiveWithGreenMana() {
        Permanent initiate = addCreatureReady(player1, new BrokersInitiate());
        addActivationMana(player1, ManaColor.GREEN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, initiate)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, initiate)).isEqualTo(5);
    }

    @Test
    @DisplayName("Can pay the hybrid mana with blue mana")
    void becomesFiveFiveWithBlueMana() {
        Permanent initiate = addCreatureReady(player1, new BrokersInitiate());
        addActivationMana(player1, ManaColor.BLUE);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, initiate)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, initiate)).isEqualTo(5);
    }

    @Test
    @DisplayName("The base power and toughness reset at end of turn")
    void resetsAtEndOfTurn() {
        Permanent initiate = addCreatureReady(player1, new BrokersInitiate());
        addActivationMana(player1, ManaColor.GREEN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, initiate)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, initiate)).isEqualTo(4);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Initiate can activate repeatedly without affecting another Initiate")
    void activatesRepeatedlyWhileTappedAndSummoningSick() {
        Permanent initiate = harness.addToBattlefieldAndReturn(player1, new BrokersInitiate());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BrokersInitiate());
        initiate.tap();
        initiate.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.getEffectivePower(gd, initiate)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, initiate)).isEqualTo(4);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, initiate)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, initiate)).isEqualTo(5);
        assertThat(initiate.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counters apply on top of the new base stats and remain after cleanup")
    void countersApplyAfterSettingBaseStats() {
        Permanent initiate = addCreatureReady(player1, new BrokersInitiate());
        initiate.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addActivationMana(player1, ManaColor.BLUE);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, initiate)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, initiate)).isEqualTo(7);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, initiate)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, initiate)).isEqualTo(6);
        assertThat(initiate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Five colorless mana cannot pay the green or blue hybrid symbol")
    void rejectsColorlessPaymentForHybridSymbol() {
        addCreatureReady(player1, new BrokersInitiate());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The hybrid mana does not replace any of the four generic mana")
    void rejectsPaymentWithOnlyFourMana() {
        addCreatureReady(player1, new BrokersInitiate());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private void addActivationMana(Player player, ManaColor hybridColor) {
        harness.addMana(player, ManaColor.COLORLESS, 4);
        harness.addMana(player, hybridColor, 1);
    }
}
