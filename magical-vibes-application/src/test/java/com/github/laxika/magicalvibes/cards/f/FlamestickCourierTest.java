package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
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

@CardUsed({FlamestickCourier.class, GoblinPiker.class, GrizzlyBears.class})
class FlamestickCourierTest extends BaseCardTest {

    @Test
    @DisplayName("The ability gives a Goblin +2/+2 and haste while Flamestick Courier remains tapped")
    void abilityBoostsGoblinWhileCourierRemainsTapped() {
        Permanent courier = addReadyCourier(player1);
        Permanent goblin = addReadyGoblin(player1);
        int basePower = gqs.getEffectivePower(gd, goblin);
        int baseToughness = gqs.getEffectiveToughness(gd, goblin);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        assertThat(courier.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(baseToughness + 2);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The boost and haste persist past the end of turn while Flamestick Courier stays tapped")
    void boostAndHastePersistPastEndOfTurn() {
        Permanent courier = addReadyCourier(player1);
        Permanent goblin = addReadyGoblin(player1);
        int basePower = gqs.getEffectivePower(gd, goblin);
        int baseToughness = gqs.getEffectiveToughness(gd, goblin);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(courier.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(baseToughness + 2);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The boost and haste end when Flamestick Courier untaps")
    void boostAndHasteEndWhenCourierUntaps() {
        Permanent courier = addReadyCourier(player1);
        Permanent goblin = addReadyGoblin(player1);
        int basePower = gqs.getEffectivePower(gd, goblin);
        int baseToughness = gqs.getEffectiveToughness(gd, goblin);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(courier.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Flamestick Courier may remain tapped during its controller's untap step")
    void courierCanRemainTapped() {
        Permanent courier = addReadyCourier(player1);
        Permanent goblin = addReadyGoblin(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(courier.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a non-Goblin creature")
    void cannotTargetNonGoblinCreature() {
        addReadyCourier(player1);
        Permanent bears = addReadyBears(player2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Goblin creature");
    }

    private Permanent addReadyCourier(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new FlamestickCourier());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addReadyGoblin(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GoblinPiker());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addReadyBears(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
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
