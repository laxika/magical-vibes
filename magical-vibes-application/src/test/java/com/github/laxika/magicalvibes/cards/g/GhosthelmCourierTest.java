package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AphettoAlchemist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhosthelmCourier.class, AphettoAlchemist.class, GrizzlyBears.class})
class GhosthelmCourierTest extends BaseCardTest {

    @Test
    @DisplayName("The ability gives a Wizard +2/+2 and shroud while Ghosthelm Courier remains tapped")
    void abilityBoostsWizardWhileCourierRemainsTapped() {
        Permanent courier = addReadyCourier(player1);
        Permanent wizard = addReadyWizard(player1);
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
        Permanent courier = addReadyCourier(player1);
        Permanent wizard = addReadyWizard(player1);
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
        Permanent courier = addReadyCourier(player1);
        Permanent wizard = addReadyWizard(player1);
        int basePower = gqs.getEffectivePower(gd, wizard);
        int baseToughness = gqs.getEffectiveToughness(gd, wizard);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, wizard.getId());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(courier.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a non-Wizard creature")
    void cannotTargetNonWizardCreature() {
        addReadyCourier(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Wizard creature");
    }

    private Permanent addReadyCourier(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GhosthelmCourier());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyWizard(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AphettoAlchemist());
        perm.setSummoningSick(false);
        return perm;
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
