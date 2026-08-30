package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Roterothopter.class)
class RoterothopterTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives +1/+0 until end of turn")
    void abilityGivesBoost() {
        Permanent thopter = addCreatureReady(player1, new Roterothopter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(thopter.getEffectivePower()).isEqualTo(1);
        assertThat(thopter.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Two activations in one turn stack to +2/+0")
    void twoActivationsStack() {
        Permanent thopter = addCreatureReady(player1, new Roterothopter());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(thopter.getEffectivePower()).isEqualTo(2);
        assertThat(thopter.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Third activation in same turn is rejected")
    void thirdActivationInSameTurnIsRejected() {
        addCreatureReady(player1, new Roterothopter());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 2 times each turn");
    }

    @Test
    @DisplayName("Activation limit resets on a new turn")
    void activationLimitResetsOnNewTurn() {
        addCreatureReady(player1, new Roterothopter());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent thopter = addCreatureReady(player1, new Roterothopter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(thopter.getEffectivePower()).isEqualTo(0);
        assertThat(thopter.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate the ability without two mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new Roterothopter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate the ability while summoning sick")
    void canActivateWithSummoningSickness() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new Roterothopter());
        thopter.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(thopter.getEffectivePower()).isEqualTo(1);
    }
}
