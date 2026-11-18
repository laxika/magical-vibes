package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SpittingDrake.class)
class SpittingDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +1/+0 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent drake = addCreatureReady(player1, new SpittingDrake());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, drake)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, drake)).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate the non-tap ability while summoning sick")
    void canActivateWithoutHasteOrTapCost() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new SpittingDrake());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, drake)).isEqualTo(1);
        assertThat(drake.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability more than once each turn")
    void cannotActivateMoreThanOncePerTurn() {
        addCreatureReady(player1, new SpittingDrake());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new SpittingDrake());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent drake = addCreatureReady(player1, new SpittingDrake());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, drake)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, drake)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, drake)).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate again on a later turn")
    void canActivateAgainOnLaterTurn() {
        Permanent drake = addCreatureReady(player1, new SpittingDrake());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, drake)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Spitting Drake can activate its ability once each turn")
    void activationLimitIsPerPermanent() {
        Permanent firstDrake = addCreatureReady(player1, new SpittingDrake());
        Permanent secondDrake = addCreatureReady(player1, new SpittingDrake());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, firstDrake)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, secondDrake)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activation limit applies before the first activation resolves")
    void cannotActivateAgainWhileAbilityIsOnStack() {
        Permanent drake = addCreatureReady(player1, new SpittingDrake());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(PermanentGrants.powerBoostOf(gd, drake)).isEqualTo(0);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");

        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, drake)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, drake)).isEqualTo(0);
    }

    @Test
    @DisplayName("Failed activation without mana does not consume the activation limit")
    void failedPaymentDoesNotConsumeActivationLimit() {
        Permanent drake = addCreatureReady(player1, new SpittingDrake());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, drake)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, drake)).isEqualTo(0);
    }

    @Test
    @DisplayName("Tapped Spitting Drake can activate its non-tap ability")
    void canActivateWhileTapped() {
        Permanent drake = addCreatureReady(player1, new SpittingDrake());
        drake.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, drake)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, drake)).isEqualTo(0);
        assertThat(drake.isTapped()).isTrue();
    }
}
