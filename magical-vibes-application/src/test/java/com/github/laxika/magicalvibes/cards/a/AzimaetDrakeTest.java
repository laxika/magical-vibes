package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AzimaetDrake.class})
class AzimaetDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Pump ability grants +1/+0 until end of turn")
    void pumpAbilityGrantsBoost() {
        Permanent drake = addCreatureReady(player1, new AzimaetDrake());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(3);
    }

    @Test
    @DisplayName("Pump ability can be activated only once each turn")
    void pumpAbilityOncePerTurn() {
        addCreatureReady(player1, new AzimaetDrake());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Pump ability does not require tapping")
    void pumpAbilityDoesNotRequireTapping() {
        Permanent drake = addCreatureReady(player1, new AzimaetDrake());
        drake.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(drake.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
    }

    @Test
    @DisplayName("Pump ability can be activated again on a new turn")
    void pumpAbilityResetsEachTurn() {
        Permanent drake = addCreatureReady(player1, new AzimaetDrake());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent drake = addCreatureReady(player1, new AzimaetDrake());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(1);
    }

    @Test
    @DisplayName("A second activation is rejected while the first is still on the stack")
    void activationLimitAppliesBeforeResolution() {
        Permanent drake = addCreatureReady(player1, new AzimaetDrake());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Drake has its own activation limit and boosts only itself")
    void separateDrakesCanEachActivate() {
        Permanent first = addCreatureReady(player1, new AzimaetDrake());
        Permanent second = addCreatureReady(player1, new AzimaetDrake());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
    }

    @Test
    @DisplayName("A summoning-sick Drake can activate its pump ability")
    void summoningSicknessDoesNotPreventActivation() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new AzimaetDrake());
        drake.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(3);
    }
}
