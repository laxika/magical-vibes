package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThoughtDevourer.class, GrizzlyBears.class, Forest.class, Mountain.class, Plains.class})
class ThoughtDevourerTest extends BaseCardTest {

    @Test
    @DisplayName("Controller must discard down to three during cleanup")
    void controllerMaximumHandSizeIsReducedByFour() {
        harness.addToBattlefield(player1, new ThoughtDevourer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new Forest(), new Mountain(), new Plains()
        )));

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.CLEANUP);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Thought Devourer does not reduce an opponent's maximum hand size")
    void opponentMaximumHandSizeIsUnaffected() {
        harness.addToBattlefield(player1, new ThoughtDevourer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Mountain(), new Plains(), new Plains()
        )));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Controller's hand size returns to seven when Thought Devourer leaves")
    void reductionEndsWhenSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new ThoughtDevourer());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Mountain(), new Plains(), new Plains()
        )));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Controller does not discard when hand is at the reduced maximum")
    void controllerDoesNotDiscardAtReducedMaximum() {
        harness.addToBattlefield(player1, new ThoughtDevourer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new Forest(), new Mountain()
        )));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Two Thought Devourers reduce the controller's maximum hand size cumulatively")
    void multipleThoughtDevourersStack() {
        harness.addToBattlefield(player1, new ThoughtDevourer());
        harness.addToBattlefield(player1, new ThoughtDevourer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }
}
