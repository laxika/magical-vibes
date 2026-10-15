package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.v.VaporSnare;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CalciteSnapper.class, Forest.class, VaporSnare.class, CunningSparkmage.class})
class CalciteSnapperTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting landfall switches Calcite Snapper's power and toughness")
    void acceptingLandfallSwitchesPowerAndToughness() {
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new CalciteSnapper());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, snapper)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, snapper)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining landfall leaves Calcite Snapper unchanged")
    void decliningLandfallLeavesStatsUnchanged() {
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new CalciteSnapper());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, snapper)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, snapper)).isEqualTo(4);
    }

    @Test
    @DisplayName("Landfall switch wears off at end of turn")
    void landfallSwitchWearsOffAtEndOfTurn() {
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new CalciteSnapper());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, snapper)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, snapper)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Calcite Snapper")
    void opponentLandDoesNotTrigger() {
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new CalciteSnapper());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gqs.getEffectivePower(gd, snapper)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, snapper)).isEqualTo(4);
    }

    @Test
    @DisplayName("A land entering without being played still triggers landfall")
    void landEnteringWithoutBeingPlayedTriggers() {
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new CalciteSnapper());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, snapper)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, snapper)).isEqualTo(1);
    }

    @Test
    @DisplayName("Accepting two landfall triggers switches power and toughness twice")
    void twoAcceptedLandfallTriggersCancelEachOther() {
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new CalciteSnapper());

        for (int i = 0; i < 2; i++) {
            harness.enterBattlefieldAndReturn(player1, new Forest());
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();

            assertThat(gqs.getEffectivePower(gd, snapper)).isEqualTo(i == 0 ? 4 : 1);
            assertThat(gqs.getEffectiveToughness(gd, snapper)).isEqualTo(i == 0 ? 1 : 4);
        }
    }

    @Test
    @DisplayName("Each Snapper independently chooses whether to switch itself")
    void multipleSnappersHaveIndependentChoices() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CalciteSnapper());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CalciteSnapper());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(List.of(gqs.getEffectivePower(gd, first), gqs.getEffectivePower(gd, second)))
                .containsExactlyInAnyOrder(1, 4);
        assertThat(gqs.getEffectivePower(gd, first) + gqs.getEffectiveToughness(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second) + gqs.getEffectiveToughness(gd, second)).isEqualTo(5);
    }

    @Test
    @DisplayName("Shroud prevents both players from targeting Calcite Snapper with an Aura")
    void shroudPreventsEitherPlayerFromTargeting() {
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new CalciteSnapper());

        for (var player : List.of(player1, player2)) {
            harness.forceActivePlayer(player);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            harness.setHand(player, List.of(new VaporSnare()));
            harness.addMana(player, ManaColor.COLORLESS, 4);
            harness.addMana(player, ManaColor.BLUE, 1);

            assertThatThrownBy(() -> harness.castEnchantment(player, 0, snapper.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("shroud");
        }
    }

    @Test
    @DisplayName("Shroud also prevents activated abilities from targeting Calcite Snapper")
    void shroudPreventsActivatedAbilityTargeting() {
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new CalciteSnapper());
        addCreatureReady(player1, new CunningSparkmage());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, snapper.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
