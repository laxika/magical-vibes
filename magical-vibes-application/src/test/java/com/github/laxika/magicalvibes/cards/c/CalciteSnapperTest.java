package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

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
}
