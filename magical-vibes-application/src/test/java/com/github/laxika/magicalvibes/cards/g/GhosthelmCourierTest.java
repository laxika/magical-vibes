package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AphettoAlchemist;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhosthelmCourier.class, AphettoAlchemist.class, ElvishWarrior.class})
class GhosthelmCourierTest extends BaseCardTest {

    @Test
    @DisplayName("The ability gives a Wizard +2/+2 and shroud while Ghosthelm Courier remains tapped")
    void abilityBoostsWizardWhileCourierRemainsTapped() {
        Permanent courier = addCreatureReady(player1, new GhosthelmCourier());
        Permanent wizard = addCreatureReady(player1, new AphettoAlchemist());
        int basePower = gqs.getEffectivePower(gd, wizard);
        int baseToughness = gqs.getEffectiveToughness(gd, wizard);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, wizard.getId());
        harness.passBothPriorities();

        assertThat(courier.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(baseToughness + 2);
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("The boost and shroud persist past the end of turn while Ghosthelm Courier stays tapped")
    void boostAndShroudPersistPastEndOfTurn() {
        Permanent courier = addCreatureReady(player1, new GhosthelmCourier());
        Permanent wizard = addCreatureReady(player1, new AphettoAlchemist());
        int basePower = gqs.getEffectivePower(gd, wizard);
        int baseToughness = gqs.getEffectiveToughness(gd, wizard);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, wizard.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(courier.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(baseToughness + 2);
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("The boost and shroud end when Ghosthelm Courier untaps")
    void boostAndShroudEndWhenCourierUntaps() {
        Permanent courier = addCreatureReady(player1, new GhosthelmCourier());
        Permanent wizard = addCreatureReady(player1, new AphettoAlchemist());
        int basePower = gqs.getEffectivePower(gd, wizard);
        int baseToughness = gqs.getEffectiveToughness(gd, wizard);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, wizard.getId());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(true);

        assertThat(courier.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("The boost and shroud persist when the controller chooses not to untap Ghosthelm Courier")
    void boostAndShroudPersistWhenCourierStaysTapped() {
        Permanent courier = addCreatureReady(player1, new GhosthelmCourier());
        Permanent wizard = addCreatureReady(player1, new AphettoAlchemist());
        int basePower = gqs.getEffectivePower(gd, wizard);
        int baseToughness = gqs.getEffectiveToughness(gd, wizard);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, wizard.getId());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(false);

        assertThat(courier.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(baseToughness + 2);
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("The ability can target an opponent's Wizard creature")
    void abilityBoostsOpponentsWizard() {
        Permanent courier = addCreatureReady(player1, new GhosthelmCourier());
        Permanent wizard = addCreatureReady(player2, new AphettoAlchemist());
        int basePower = gqs.getEffectivePower(gd, wizard);
        int baseToughness = gqs.getEffectiveToughness(gd, wizard);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, wizard.getId());
        harness.passBothPriorities();

        assertThat(courier.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(baseToughness + 2);
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a non-Wizard creature")
    void cannotTargetNonWizardCreature() {
        addCreatureReady(player1, new GhosthelmCourier());
        Permanent warrior = addCreatureReady(player2, new ElvishWarrior());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, warrior.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Wizard creature");
    }

    private void advanceToNextTurnWithMayChoice(boolean acceptUntap) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UNTAP);
        harness.handleMayAbilityChosen(player1, acceptUntap);
    }
}
