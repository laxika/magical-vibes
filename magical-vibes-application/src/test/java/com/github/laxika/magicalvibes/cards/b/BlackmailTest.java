package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlackmailTest extends BaseCardTest {

    private PendingInteraction.RevealCardsDiscardChoice activeChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack targeting a player")
    void castingTargetsPlayer() {
        harness.setHand(player1, List.of(new Blackmail()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    // ===== Reveal stage (hand larger than three) =====

    @Test
    @DisplayName("Target player chooses which three cards to reveal")
    void targetChoosesThreeToReveal() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new Peek(), new HornedTurtle(), new Forest())));
        harness.setHand(player1, List.of(new Blackmail()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Reveal stage: the target (player2) decides.
        PendingInteraction.RevealCardsDiscardChoice choice = activeChoice();
        assertThat(choice).isNotNull();
        assertThat(choice.revealStage()).isTrue();
        assertThat(choice.decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(choice.remainingCount()).isEqualTo(3);
        assertThat(choice.validIndices()).containsExactly(0, 1, 2, 3);
    }

    @Test
    @DisplayName("Controller discards one of the three revealed cards; others stay in hand")
    void controllerDiscardsOneRevealed() {
        Card bears = new GrizzlyBears();
        Card peek = new Peek();
        Card turtle = new HornedTurtle();
        Card forest = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(bears, peek, turtle, forest)));
        harness.setHand(player1, List.of(new Blackmail()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Target reveals Grizzly Bears (0), Peek (1), Horned Turtle (2) — Forest stays hidden.
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 2);

        // Now the controller chooses one of the three revealed cards.
        PendingInteraction.RevealCardsDiscardChoice discardChoice = activeChoice();
        assertThat(discardChoice).isNotNull();
        assertThat(discardChoice.revealStage()).isFalse();
        assertThat(discardChoice.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(discardChoice.validIndices()).containsExactly(0, 1, 2);

        // Controller makes player2 discard the second revealed card (Peek).
        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getName().equals("Peek"));
        // The other revealed cards and the unrevealed Forest remain in hand.
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Horned Turtle", "Forest");
    }

    @Test
    @DisplayName("Controller cannot choose during the reveal stage")
    void controllerCannotChooseDuringRevealStage() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new Peek(), new HornedTurtle(), new Forest())));
        harness.setHand(player1, List.of(new Blackmail()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    // ===== Whole hand revealed (three or fewer cards) =====

    @Test
    @DisplayName("With three or fewer cards the whole hand is revealed, no reveal choice")
    void wholeHandRevealedWhenThreeOrFewer() {
        Card bears = new GrizzlyBears();
        Card peek = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(bears, peek)));
        harness.setHand(player1, List.of(new Blackmail()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Straight to the controller's discard choice over the whole hand.
        PendingInteraction.RevealCardsDiscardChoice choice = activeChoice();
        assertThat(choice).isNotNull();
        assertThat(choice.revealStage()).isFalse();
        assertThat(choice.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.validIndices()).containsExactly(0, 1);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Peek");
    }

    @Test
    @DisplayName("Resolving against an empty hand does nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Blackmail()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("empty"));
    }

    // ===== Validation =====

    @Test
    @DisplayName("Invalid revealed-card index is rejected in the discard stage")
    void invalidDiscardIndexRejected() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        harness.setHand(player1, List.of(new Blackmail()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    // ===== Logging =====

    @Test
    @DisplayName("Revealed cards and the discard are logged")
    void revealAndDiscardLogged() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setHand(player1, List.of(new Blackmail()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("reveals") && log.contains("Grizzly Bears"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("discards") && log.contains("Grizzly Bears"));
    }
}
