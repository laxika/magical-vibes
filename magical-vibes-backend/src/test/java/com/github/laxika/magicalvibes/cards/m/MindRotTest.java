package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MindRotTest {

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
    @DisplayName("Mind Rot has correct card properties")
    void hasCorrectProperties() {
        MindRot card = new MindRot();

        assertThat(card.getName()).isEqualTo("Mind Rot");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{2}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(TargetPlayerDiscardsEffect.class);
        TargetPlayerDiscardsEffect effect = (TargetPlayerDiscardsEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.amount()).isEqualTo(2);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Mind Rot puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Mind Rot");
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getTargetPermanentId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving — target has 2+ cards =====

    @Test
    @DisplayName("Resolving prompts target player to discard")
    void resolvingPromptsTargetPlayerToDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest())));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Target player (player2) should be prompted to discard, NOT the caster
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.awaitingCardChoicePlayerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.discardRemainingCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Target discards two cards of their choice")
    void targetDiscardsTwoCards() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest())));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Target player chooses first discard
        harness.handleCardChosen(player2, 0); // discard Grizzly Bears

        // Still awaiting second discard
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.awaitingCardChoicePlayerId()).isEqualTo(player2.getId());

        // Target player chooses second discard
        harness.handleCardChosen(player2, 0); // discard Peek (now at index 0)

        // Discard complete
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Forest");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Peek"));
    }

    @Test
    @DisplayName("Target can choose any cards including lands")
    void targetCanChooseLands() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new GrizzlyBears(), new Forest())));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // All indices should be valid — Mind Rot doesn't restrict card types
        assertThat(gd.interaction.awaitingCardChoiceValidIndices()).containsExactlyInAnyOrder(0, 1, 2);

        harness.handleCardChosen(player2, 0); // discard Forest
        harness.handleCardChosen(player2, 0); // discard GrizzlyBears (now index 0)

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Mind Rot goes to caster's graveyard after resolving")
    void goesToCasterGraveyardAfterResolving() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mind Rot"));
    }

    // ===== Resolving — target has exactly 1 card =====

    @Test
    @DisplayName("Target with one card discards it then discard ends")
    void targetWithOneCardDiscardsIt() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);

        harness.handleCardChosen(player2, 0); // discard the only card

        // Hand is now empty, so the second discard is skipped
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Resolving — target has empty hand =====

    @Test
    @DisplayName("Target with empty hand results in no discard prompt")
    void targetWithEmptyHandNoPrompt() {
        harness.setHand(player2, new ArrayList<>(List.of()));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // No discard prompt — hand is empty
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no cards to discard"));
    }

    // ===== Targeting self =====

    @Test
    @DisplayName("Can target yourself to discard your own cards")
    void canTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new MindRot(), new GrizzlyBears(), new Peek(), new Forest())));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        // Player1 is prompted to discard from their own hand
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.awaitingCardChoicePlayerId()).isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0); // discard GrizzlyBears
        harness.handleCardChosen(player1, 0); // discard Peek

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    // ===== Wrong player cannot choose =====

    @Test
    @DisplayName("Caster cannot make the discard choice for the target")
    void casterCannotChooseForTarget() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    // ===== Discard logging =====

    @Test
    @DisplayName("Discarded cards are logged")
    void discardIsLogged() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("discards") && log.contains("Grizzly Bears"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("discards") && log.contains("Peek"));
    }
}


