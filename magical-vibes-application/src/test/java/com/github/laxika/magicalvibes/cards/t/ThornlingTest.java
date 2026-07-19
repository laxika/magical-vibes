package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThornlingTest extends BaseCardTest {

    // ===== {G}: gains haste until end of turn (ability 0) =====

    @Test
    @DisplayName("{G} grants haste until end of turn")
    void grantsHaste() {
        Permanent thornling = addCreatureReady(player1, new Thornling());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(thornling.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Haste wears off at end of turn")
    void hasteWearsOff() {
        Permanent thornling = addCreatureReady(player1, new Thornling());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(thornling.getGrantedKeywords()).contains(Keyword.HASTE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(thornling.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    // ===== {G}: gains trample until end of turn (ability 1) =====

    @Test
    @DisplayName("{G} grants trample until end of turn")
    void grantsTrample() {
        Permanent thornling = addCreatureReady(player1, new Thornling());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(thornling.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    // ===== {G}: gains indestructible until end of turn (ability 2) =====

    @Test
    @DisplayName("{G} grants indestructible until end of turn")
    void grantsIndestructible() {
        Permanent thornling = addCreatureReady(player1, new Thornling());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(thornling.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
    }

    // ===== {1}: +1/-1 until end of turn (ability 3) =====

    @Test
    @DisplayName("{1} gives +1/-1 until end of turn")
    void givesPlusOneMinusOne() {
        Permanent thornling = addCreatureReady(player1, new Thornling());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(thornling.getPowerModifier()).isEqualTo(1);
        assertThat(thornling.getToughnessModifier()).isEqualTo(-1);
    }

    // ===== {1}: -1/+1 until end of turn (ability 4) =====

    @Test
    @DisplayName("{1} gives -1/+1 until end of turn")
    void givesMinusOnePlusOne() {
        Permanent thornling = addCreatureReady(player1, new Thornling());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 4, null, null);
        harness.passBothPriorities();

        assertThat(thornling.getPowerModifier()).isEqualTo(-1);
        assertThat(thornling.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Stat modifiers reset at end of turn")
    void boostWearsOff() {
        Permanent thornling = addCreatureReady(player1, new Thornling());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();
        assertThat(thornling.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(thornling.getPowerModifier()).isEqualTo(0);
        assertThat(thornling.getToughnessModifier()).isEqualTo(0);
    }
}
