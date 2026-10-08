package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TorchDrake.class)
class TorchDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{R}: Torch Drake gets +1/+0 until end of turn")
    void boostsPower() {
        Permanent drake = addCreatureReady(player1, new TorchDrake());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(2);
    }

    @Test
    @DisplayName("Torch Drake's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent drake = addCreatureReady(player1, new TorchDrake());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(2);
    }

    @Test
    @DisplayName("Torch Drake requires red mana for its activated ability")
    void requiresRedMana() {
        addCreatureReady(player1, new TorchDrake());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Torch Drake's ability can be activated multiple times in one turn")
    void canActivateMultipleTimesInOneTurn() {
        Permanent drake = addCreatureReady(player1, new TorchDrake());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(2);
    }

    @Test
    @DisplayName("Torch Drake can activate its non-tap ability while summoning sick")
    void canActivateWhileSummoningSick() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new TorchDrake());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(3);
    }

    @Test
    @DisplayName("Torch Drake can pay the generic portion with nonred mana")
    void acceptsMixedManaPayment() {
        Permanent drake = addCreatureReady(player1, new TorchDrake());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(2);
    }

    @Test
    @DisplayName("Torch Drake can activate while tapped and remains tapped")
    void canActivateWhileTapped() {
        Permanent drake = addCreatureReady(player1, new TorchDrake());
        drake.tap();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(3);
        assertThat(drake.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Torch Drake's boost uses the stack and affects only its source")
    void boostsOnlySourceOnResolution() {
        Permanent first = addCreatureReady(player1, new TorchDrake());
        Permanent second = addCreatureReady(player1, new TorchDrake());
        Permanent opposing = addCreatureReady(player2, new TorchDrake());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(2);
    }
}
