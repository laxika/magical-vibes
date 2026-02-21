package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpellbookTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Spellbook has correct card properties")
    void hasCorrectProperties() {
        Spellbook card = new Spellbook();

        assertThat(card.getName()).isEqualTo("Spellbook");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{0}");
        assertThat(card.getColor()).isNull();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(NoMaximumHandSizeEffect.class);
    }

    // ===== Hand limit enforcement =====

    @Test
    @DisplayName("Player with more than 7 cards must discard during cleanup")
    void handLimitEnforcedDuringCleanup() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        // Give player1 a hand of 9 cards
        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Forest(),
                new Mountain(), new Mountain(), new Plains()
        )));

        // Advance to cleanup
        harness.getGameService().advanceStep(gd);

        // Player should be prompted to discard 2 cards (9 - 7 = 2)
        assertThat(gd.currentStep).isEqualTo(TurnStep.CLEANUP);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.awaitingCardChoicePlayerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.discardRemainingCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Player discards down to 7 cards during cleanup")
    void playerDiscardsDownToSeven() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Forest(),
                new Mountain(), new Mountain(), new Plains()
        )));

        harness.getGameService().advanceStep(gd);

        // Discard first card
        harness.handleCardChosen(player1, 0);
        // Discard second card
        harness.handleCardChosen(player1, 0);

        // Discard complete, hand is now 7
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("No discard prompt when hand size is exactly 7")
    void noDiscardAtExactlySeven() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Forest(), new Mountain()
        )));

        harness.getGameService().advanceStep(gd);

        // No discard needed
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
    }

    @Test
    @DisplayName("No discard prompt when hand size is below 7")
    void noDiscardBelowSeven() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Discarded cards go to graveyard")
    void discardedCardsGoToGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(),
                new Mountain(), new Mountain()
        )));

        harness.getGameService().advanceStep(gd);

        // Discard the first card (Grizzly Bears at index 0)
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
    }

    // ===== Spellbook bypasses hand limit =====

    @Test
    @DisplayName("Spellbook on battlefield prevents hand limit enforcement")
    void spellbookPreventsHandLimit() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new Spellbook());

        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Forest(),
                new Mountain(), new Mountain(), new Plains()
        )));

        harness.getGameService().advanceStep(gd);

        // No discard prompt — Spellbook removes hand size limit
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    // ===== Only controller benefits =====

    @Test
    @DisplayName("Opponent's Spellbook does not remove your hand limit")
    void opponentSpellbookDoesNotHelp() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        // Spellbook is on opponent's battlefield
        harness.addToBattlefield(player2, new Spellbook());

        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Forest(),
                new Mountain(), new Mountain(), new Plains()
        )));

        harness.getGameService().advanceStep(gd);

        // Player1 must still discard — opponent's Spellbook doesn't help
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.awaitingCardChoicePlayerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.discardRemainingCount()).isEqualTo(2);
    }

    // ===== Spellbook removal =====

    @Test
    @DisplayName("Hand limit enforced after Spellbook is removed")
    void handLimitEnforcedAfterSpellbookRemoved() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new Spellbook());

        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Forest(),
                new Mountain(), new Mountain(), new Plains()
        )));

        // Remove Spellbook before cleanup
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.getGameService().advanceStep(gd);

        // Without Spellbook, must discard
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.discardRemainingCount()).isEqualTo(2);
    }

    // ===== Cleanup discard is logged =====

    @Test
    @DisplayName("Cleanup discard is logged")
    void cleanupDiscardIsLogged() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(),
                new Mountain(), new Mountain()
        )));

        harness.getGameService().advanceStep(gd);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("discards") && log.contains("Grizzly Bears"));
    }
}
