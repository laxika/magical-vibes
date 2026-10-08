package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(StalkingDrone.class)
class StalkingDroneTest extends BaseCardTest {

    @Test
    void colorlessManaBoostsStalkingDrone() {
        Permanent drone = addReadyDrone();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drone)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drone)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void abilityCanBeActivatedOnlyOnceEachTurn() {
        addReadyDrone();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent drone = addReadyDrone();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drone)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drone)).isEqualTo(2);
    }

    @Test
    void coloredManaCannotPayColorlessActivationCost() {
        Permanent drone = addReadyDrone();
        for (ManaColor color : ManaColor.values()) {
            if (color != ManaColor.COLORLESS) {
                harness.addMana(player1, color, 1);
            }
        }

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, drone)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drone)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drone)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drone)).isEqualTo(4);
    }

    @Test
    void tappedSummoningSickDroneCanActivate() {
        Permanent drone = harness.addToBattlefieldAndReturn(player1, new StalkingDrone());
        drone.setSummoningSick(true);
        drone.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drone)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drone)).isEqualTo(4);
        assertThat(drone.isTapped()).isTrue();
    }

    @Test
    void activationLimitAppliesBeforeAbilityResolves() {
        Permanent drone = addReadyDrone();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.getEffectivePower(gd, drone)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drone)).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drone)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drone)).isEqualTo(4);
    }

    @Test
    void eachDroneCanActivateIndependently() {
        Permanent first = addReadyDrone();
        Permanent second = addReadyDrone();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    void abilityCanActivateAgainOnOpponentsTurn() {
        Permanent drone = addReadyDrone();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gqs.getEffectivePower(gd, drone)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drone)).isEqualTo(4);
    }

    private Permanent addReadyDrone() {
        return addCreatureReady(player1, new StalkingDrone());
    }
}
