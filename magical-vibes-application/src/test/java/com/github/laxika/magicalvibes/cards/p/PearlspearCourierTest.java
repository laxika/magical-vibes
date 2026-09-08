package com.github.laxika.magicalvibes.cards.p;

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

@CardUsed({PearlspearCourier.class, GrizzlyBears.class})
class PearlspearCourierTest extends BaseCardTest {

    @Test
    @DisplayName("The ability gives a Soldier +2/+2 and vigilance while Pearlspear Courier remains tapped")
    void abilityBoostsSoldierWhileCourierRemainsTapped() {
        Permanent courier = addReadyCourier(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, courier.getId());
        harness.passBothPriorities();

        assertThat(courier.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, courier)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, courier)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, courier, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The boost persists past the end of turn while Pearlspear Courier stays tapped")
    void boostPersistsPastEndOfTurn() {
        Permanent courier = addReadyCourier(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, courier.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, courier)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, courier, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The boost ends when Pearlspear Courier untaps")
    void boostEndsWhenCourierUntaps() {
        Permanent courier = addReadyCourier(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, courier.getId());
        harness.passBothPriorities();

        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(courier.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, courier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, courier)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, courier, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a non-Soldier creature")
    void cannotTargetNonSoldierCreature() {
        addReadyCourier(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Soldier creature");
    }

    private Permanent addReadyCourier(Player player) {
        return addCreatureReady(player, new PearlspearCourier());
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
