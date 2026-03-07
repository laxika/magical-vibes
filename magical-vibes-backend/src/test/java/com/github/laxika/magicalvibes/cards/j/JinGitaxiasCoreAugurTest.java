package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOpponentMaxHandSizeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JinGitaxiasCoreAugurTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Jin-Gitaxias has correct effects")
    void hasCorrectEffects() {
        JinGitaxiasCoreAugur card = new JinGitaxiasCoreAugur();

        assertThat(card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst())
                .isInstanceOf(DrawCardEffect.class);
        DrawCardEffect draw = (DrawCardEffect) card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst();
        assertThat(draw.amount()).isEqualTo(7);

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(ReduceOpponentMaxHandSizeEffect.class);
        ReduceOpponentMaxHandSizeEffect reduce = (ReduceOpponentMaxHandSizeEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(reduce.reduction()).isEqualTo(7);
    }

    // ===== End step draw trigger =====

    @Test
    @DisplayName("Draws 7 cards at controller's end step")
    void drawsSevenAtControllerEndStep() {
        harness.addToBattlefield(player1, new JinGitaxiasCoreAugur());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.SECOND_MAIN);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        // Advance to end step (triggers end step abilities)
        gs.advanceStep(gd);

        // The draw trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Jin-Gitaxias, Core Augur");

        // Resolve the trigger
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 7);
    }

    @Test
    @DisplayName("Does not draw on opponent's end step")
    void doesNotDrawOnOpponentEndStep() {
        harness.addToBattlefield(player1, new JinGitaxiasCoreAugur());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.SECOND_MAIN);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        // Advance to end step on opponent's turn
        gs.advanceStep(gd);

        // No trigger should fire for player1's Jin-Gitaxias
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    // ===== Opponent max hand size reduction =====

    @Test
    @DisplayName("Opponent must discard down to 0 during cleanup with Jin-Gitaxias on battlefield")
    void opponentDiscardsToZero() {
        harness.addToBattlefield(player1, new JinGitaxiasCoreAugur());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        // Give opponent 3 cards in hand
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new Forest(), new Mountain()
        )));

        // Advance to cleanup
        gs.advanceStep(gd);

        // Opponent should be prompted to discard all 3 cards (max hand size = 7 - 7 = 0)
        assertThat(gd.currentStep).isEqualTo(TurnStep.CLEANUP);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.awaitingCardChoicePlayerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.discardRemainingCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Controller's hand size is not affected by own Jin-Gitaxias")
    void controllerHandSizeUnaffected() {
        harness.addToBattlefield(player1, new JinGitaxiasCoreAugur());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        // Give controller exactly 8 cards (normally must discard 1)
        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Forest(),
                new Mountain(), new Plains()
        )));

        // Advance to cleanup
        gs.advanceStep(gd);

        // Controller should discard down to 7 (normal hand size, not reduced)
        assertThat(gd.currentStep).isEqualTo(TurnStep.CLEANUP);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.discardRemainingCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("No discard required for opponent with empty hand")
    void noDiscardForEmptyHand() {
        harness.addToBattlefield(player1, new JinGitaxiasCoreAugur());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        // Opponent has no cards
        harness.setHand(player2, new ArrayList<>());

        gs.advanceStep(gd);

        // No discard needed
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.DISCARD_CHOICE);
    }

    @Test
    @DisplayName("Hand size reduction is removed when Jin-Gitaxias leaves battlefield")
    void handSizeRestoredWhenRemoved() {
        harness.addToBattlefield(player1, new JinGitaxiasCoreAugur());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        // Give opponent 3 cards
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new Forest(), new Mountain()
        )));

        // Remove Jin-Gitaxias before cleanup
        gd.playerBattlefields.get(player1.getId()).clear();

        gs.advanceStep(gd);

        // No discard needed — max hand size is back to 7
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }
}
