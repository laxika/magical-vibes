package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Thornling.class})
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
    @Test
    @DisplayName("Trample and indestructible expire at end of turn")
    void remainingKeywordsWearOff() {
        Permanent thornling = addCreatureReady(player1, new Thornling());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(thornling.getGrantedKeywords()).contains(Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(thornling.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Repeated stat changes accumulate and affect only their source")
    void statChangesAccumulateOnlyOnSource() {
        Permanent thornling = addCreatureReady(player1, new Thornling());
        Permanent other = addCreatureReady(player1, new Thornling());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, 4, null, null);
            harness.passBothPriorities();
        }
        assertThat(thornling.getPowerModifier()).isEqualTo(-2);
        assertThat(thornling.getToughnessModifier()).isEqualTo(2);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(thornling.getPowerModifier()).isEqualTo(-1);
        assertThat(thornling.getToughnessModifier()).isEqualTo(1);
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(thornling.getPowerModifier()).isZero();
        assertThat(thornling.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Indestructible does not prevent dying from zero toughness")
    void indestructibleDoesNotPreventZeroToughnessDeath() {
        addCreatureReady(player1, new Thornling());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, 3, null, null);
            harness.passBothPriorities();
            if (i < 3) {
                harness.assertOnBattlefield(player1, "Thornling");
            }
        }

        harness.assertNotOnBattlefield(player1, "Thornling");
        harness.assertInGraveyard(player1, "Thornling");
    }
}
