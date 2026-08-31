package com.github.laxika.magicalvibes.cards.e;

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

@CardUsed({EvergloveCourier.class, GrizzlyBears.class})
class EvergloveCourierTest extends BaseCardTest {

    @Test
    @DisplayName("The ability gives an Elf +2/+2 and trample while Everglove Courier remains tapped")
    void abilityBoostsElfWhileCourierRemainsTapped() {
        Permanent courier = addReadyCourier(player1);
        int basePower = gqs.getEffectivePower(gd, courier);
        int baseToughness = gqs.getEffectiveToughness(gd, courier);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, courier.getId());
        harness.passBothPriorities();

        assertThat(courier.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, courier)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, courier)).isEqualTo(baseToughness + 2);
        assertThat(gqs.hasKeyword(gd, courier, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The boost and trample persist past the end of turn while Everglove Courier stays tapped")
    void boostAndTramplePersistPastEndOfTurn() {
        Permanent courier = addReadyCourier(player1);
        int basePower = gqs.getEffectivePower(gd, courier);
        int baseToughness = gqs.getEffectiveToughness(gd, courier);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, courier.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(courier.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, courier)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, courier)).isEqualTo(baseToughness + 2);
        assertThat(gqs.hasKeyword(gd, courier, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The boost and trample end when Everglove Courier untaps")
    void boostAndTrampleEndWhenCourierUntaps() {
        Permanent courier = addReadyCourier(player1);
        int basePower = gqs.getEffectivePower(gd, courier);
        int baseToughness = gqs.getEffectiveToughness(gd, courier);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, courier.getId());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(courier.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, courier)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, courier)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, courier, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a non-Elf creature")
    void cannotTargetNonElfCreature() {
        addReadyCourier(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an Elf creature");
    }

    private Permanent addReadyCourier(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new EvergloveCourier());
        perm.setSummoningSick(false);
        return perm;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
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
