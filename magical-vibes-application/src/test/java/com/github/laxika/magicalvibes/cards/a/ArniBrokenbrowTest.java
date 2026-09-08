package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArniBrokenbrowTest extends BaseCardTest {

    @Test
    @DisplayName("Boast sets Arni's base power to one plus the greatest other creature's actual power")
    void boastUsesGreatestActualPowerAtResolution() {
        Permanent arni = addCreatureReady(player1, new ArniBrokenbrow());
        GrizzlyBears otherCard = new GrizzlyBears();
        otherCard.setPower(3);
        otherCard.setToughness(5);
        Permanent other = addCreatureReady(player1, otherCard);
        arni.setAttackedThisTurn(true);
        arni.setPowerModifier(1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        other.setPowerModifier(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, arni)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, arni)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boast can set Arni's base power below zero")
    void boastUsesNegativeGreatestPower() {
        Permanent arni = addCreatureReady(player1, new ArniBrokenbrow());
        GrizzlyBears otherCard = new GrizzlyBears();
        otherCard.setPower(-3);
        otherCard.setToughness(5);
        addCreatureReady(player1, otherCard);
        arni.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, arni)).isEqualTo(-2);
    }

    @Test
    @DisplayName("Declining Arni's boast leaves its power unchanged")
    void boastIsOptional() {
        Permanent arni = addCreatureReady(player1, new ArniBrokenbrow());
        arni.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, arni)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boast can be activated only after attacking and only once each turn")
    void boastHasAttackAndOncePerTurnRestrictions() {
        Permanent arni = addCreatureReady(player1, new ArniBrokenbrow());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");

        arni.setAttackedThisTurn(true);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Boast's base-power change wears off at end of turn")
    void boastWearsOffAtEndOfTurn() {
        Permanent arni = addCreatureReady(player1, new ArniBrokenbrow());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        other.setPowerModifier(2);
        arni.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gqs.getEffectivePower(gd, arni)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, arni)).isEqualTo(3);
    }
}
