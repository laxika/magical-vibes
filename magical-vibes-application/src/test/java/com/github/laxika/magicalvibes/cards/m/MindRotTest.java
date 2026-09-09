package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.w.WildGriffin;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindRot.class, Forest.class, WildGriffin.class, LavaAxe.class})
class MindRotTest extends BaseCardTest {

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
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
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
        harness.setHand(player2, List.of(new WildGriffin(), new LavaAxe(), new Forest()));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Target player (player2) should be prompted to discard, NOT the caster
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Target discards two cards of their choice")
    void targetDiscardsTwoCards() {
        harness.setHand(player2, List.of(new WildGriffin(), new LavaAxe(), new Forest()));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Target player chooses first discard
        harness.handleCardChosen(player2, 0); // discard Wild Griffin

        // Still awaiting second discard
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());

        // Target player chooses second discard
        harness.handleCardChosen(player2, 0); // discard Lava Axe (now at index 0)

        // Discard complete
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Forest");
        harness.assertInGraveyard(player2, "Wild Griffin");
        harness.assertInGraveyard(player2, "Lava Axe");
    }

    @Test
    @DisplayName("Target can choose any cards including lands")
    void targetCanChooseLands() {
        harness.setHand(player2, List.of(new Forest(), new WildGriffin(), new Forest()));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // All indices should be valid — Mind Rot doesn't restrict card types
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactlyInAnyOrder(0, 1, 2);

        harness.handleCardChosen(player2, 0); // discard Forest
        harness.handleCardChosen(player2, 0); // discard Wild Griffin (now index 0)

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Mind Rot goes to caster's graveyard after resolving")
    void goesToCasterGraveyardAfterResolving() {
        harness.setHand(player2, List.of(new WildGriffin(), new LavaAxe()));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Mind Rot");
    }

    // ===== Resolving — target has exactly 1 card =====

    @Test
    @DisplayName("Target with one card discards it then discard ends")
    void targetWithOneCardDiscardsIt() {
        harness.setHand(player2, List.of(new WildGriffin()));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player2, 0); // discard the only card

        // Hand is now empty, so the second discard is skipped
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Wild Griffin");
    }

    // ===== Resolving — target has empty hand =====

    @Test
    @DisplayName("Target with empty hand results in no discard prompt")
    void targetWithEmptyHandNoPrompt() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // No discard prompt — hand is empty
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no cards to discard"));
    }

    // ===== Targeting self =====

    @Test
    @DisplayName("Can target yourself to discard your own cards")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new MindRot(), new WildGriffin(), new LavaAxe(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        // Player1 is prompted to discard from their own hand
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0); // discard Wild Griffin
        harness.handleCardChosen(player1, 0); // discard Lava Axe

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    // ===== Wrong player cannot choose =====

    @Test
    @DisplayName("Caster cannot make the discard choice for the target")
    void casterCannotChooseForTarget() {
        harness.setHand(player2, List.of(new WildGriffin(), new LavaAxe()));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    // ===== Discard logging =====

    @Test
    @DisplayName("Discarded cards are logged")
    void discardIsLogged() {
        harness.setHand(player2, List.of(new WildGriffin(), new LavaAxe()));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("discards") && log.contains("Wild Griffin"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("discards") && log.contains("Lava Axe"));
    }
}

