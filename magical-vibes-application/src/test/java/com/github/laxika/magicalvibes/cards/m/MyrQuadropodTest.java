package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EnsouledScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MyrQuadropod.class, EnsouledScimitar.class})
class MyrQuadropodTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability switches power and toughness")
    void switchesPowerAndToughness() {
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new MyrQuadropod());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(1);
    }

    @Test
    @DisplayName("The switch wears off at cleanup")
    void switchWearsOff() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new MyrQuadropod());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(4);
    }

    @Test
    @DisplayName("Two activations cancel each other")
    void twoActivationsCancelEachOther() {
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new MyrQuadropod());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(4);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Myr can switch on an opponent's turn")
    void activatesWhileTappedOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new MyrQuadropod());
        myr.setSummoningSick(true);
        myr.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(1);
        assertThat(myr.isTapped()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Equipment modifiers apply before the switch regardless of resolution order")
    void equipmentModifiersApplyBeforeSwitch(boolean equipFirst) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new MyrQuadropod());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new EnsouledScimitar());

        if (equipFirst) {
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.activateAbility(player1, 1, 1, null, myr.getId());
            harness.passBothPriorities();
        }

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        if (!equipFirst) {
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.activateAbility(player1, 1, 1, null, myr.getId());
            harness.passBothPriorities();
        }

        assertThat(scimitar.getAttachedTo()).isEqualTo(myr.getId());
        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(9);
    }
}
