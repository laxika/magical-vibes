package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(FireDrake.class)
class FireDrakeTest extends BaseCardTest {

    @Test
    void cannotActivateAgainWhileFirstActivationIsOnStack() {
        Permanent drake = addCreatureReady(player1, new FireDrake());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(drake.getPowerModifier()).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");

        harness.passBothPriorities();
        assertThat(drake.getPowerModifier()).isEqualTo(1);
    }

    @Test
    void eachDrakeCanActivateOnceIndependently() {
        Permanent first = addCreatureReady(player1, new FireDrake());
        Permanent second = addCreatureReady(player1, new FireDrake());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(second.getPowerModifier()).isZero();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(second.getPowerModifier()).isEqualTo(1);
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new FireDrake());
        drake.setSummoningSick(true);
        drake.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(drake.getPowerModifier()).isEqualTo(1);
        assertThat(drake.getToughnessModifier()).isZero();
        assertThat(drake.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability gives +1/+0 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent drake = addCreatureReady(player1, new FireDrake());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(drake.getPowerModifier()).isEqualTo(1);
        assertThat(drake.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate ability more than once each turn")
    void cannotActivateMoreThanOncePerTurn() {
        addCreatureReady(player1, new FireDrake());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent drake = addCreatureReady(player1, new FireDrake());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(drake.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(drake.getPowerModifier()).isEqualTo(0);
        assertThat(drake.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    void canActivateAgainOnLaterTurn() {
        Permanent drake = addCreatureReady(player1, new FireDrake());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(drake.getPowerModifier()).isEqualTo(1);
    }
}
