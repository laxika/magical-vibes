package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(HermiticNautilus.class)
class HermiticNautilusTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives this creature +3/-3 until end of turn")
    void activationBoostsSelf() {
        Permanent nautilus = addReadyNautilus();
        int basePower = gqs.getEffectivePower(gd, nautilus);
        int baseToughness = gqs.getEffectiveToughness(gd, nautilus);
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, nautilus)).isEqualTo(basePower + 3);
        assertThat(gqs.getEffectiveToughness(gd, nautilus)).isEqualTo(baseToughness - 3);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent nautilus = addReadyNautilus();
        int basePower = gqs.getEffectivePower(gd, nautilus);
        int baseToughness = gqs.getEffectiveToughness(gd, nautilus);
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, nautilus)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, nautilus)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("The ability can be activated while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent nautilus = harness.addToBattlefieldAndReturn(player1, new HermiticNautilus());
        nautilus.setSummoningSick(true);
        nautilus.tap();
        int basePower = gqs.getEffectivePower(gd, nautilus);
        int baseToughness = gqs.getEffectiveToughness(gd, nautilus);
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, nautilus)).isEqualTo(basePower + 3);
        assertThat(gqs.getEffectiveToughness(gd, nautilus)).isEqualTo(baseToughness - 3);
        assertThat(nautilus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A second activation reduces toughness below zero and puts the creature in the graveyard")
    void secondActivationKillsNautilus() {
        addReadyNautilus();
        addActivationMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hermitic Nautilus");
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hermitic Nautilus");
        harness.assertInGraveyard(player1, "Hermitic Nautilus");
    }

    @Test
    @DisplayName("The boost affects only the source creature")
    void boostDoesNotAffectOtherCreatures() {
        addReadyNautilus();
        Permanent other = harness.addToBattlefieldAndReturn(player1, new HermiticNautilus());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HermiticNautilus());
        int otherPower = gqs.getEffectivePower(gd, other);
        int otherToughness = gqs.getEffectiveToughness(gd, other);
        int opponentPower = gqs.getEffectivePower(gd, opponent);
        int opponentToughness = gqs.getEffectiveToughness(gd, opponent);
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(otherPower);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(otherToughness);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(opponentPower);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(opponentToughness);
    }

    private Permanent addReadyNautilus() {
        return addCreatureReady(player1, new HermiticNautilus());
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
