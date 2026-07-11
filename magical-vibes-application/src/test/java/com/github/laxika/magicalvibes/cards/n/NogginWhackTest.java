package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NogginWhackTest extends BaseCardTest {

    private PendingInteraction.RevealCardsDiscardChoice activeChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
    }

    @Test
    @DisplayName("Controller chooses two of the three revealed cards to discard")
    void controllerDiscardsTwoRevealed() {
        Card bears = new GrizzlyBears();
        Card peek = new Peek();
        Card turtle = new HornedTurtle();
        Card forest = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(bears, peek, turtle, forest)));
        harness.setHand(player1, List.of(new NogginWhack()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Target reveals Grizzly Bears (0), Peek (1), Horned Turtle (2) — Forest stays hidden.
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 2);

        // Controller must pick two of the three revealed cards.
        PendingInteraction.RevealCardsDiscardChoice discardChoice = activeChoice();
        assertThat(discardChoice).isNotNull();
        assertThat(discardChoice.revealStage()).isFalse();
        assertThat(discardChoice.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(discardChoice.remainingCount()).isEqualTo(2);
        assertThat(discardChoice.validIndices()).containsExactly(0, 1, 2);

        // Discard the first revealed card (Grizzly Bears); still one more to choose.
        harness.handleCardChosen(player1, 0);
        PendingInteraction.RevealCardsDiscardChoice second = activeChoice();
        assertThat(second).isNotNull();
        assertThat(second.remainingCount()).isEqualTo(1);
        assertThat(second.revealedCardIds()).hasSize(2);

        // Discard Horned Turtle (index 1 of the two remaining revealed cards).
        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Horned Turtle");
        // Peek (revealed, not chosen) and the hidden Forest remain in hand.
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Peek", "Forest");
    }

    @Test
    @DisplayName("With exactly two cards the whole hand is revealed and both are discarded")
    void wholeHandOfTwoBothDiscarded() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        harness.setHand(player1, List.of(new NogginWhack()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Straight to the controller's discard choice over the whole (two-card) hand.
        PendingInteraction.RevealCardsDiscardChoice choice = activeChoice();
        assertThat(choice).isNotNull();
        assertThat(choice.revealStage()).isFalse();
        assertThat(choice.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Peek");
    }

    @Test
    @DisplayName("With a single card only that card is discarded (fewer than the discard count)")
    void singleCardDiscardsOnlyOne() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setHand(player1, List.of(new NogginWhack()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealCardsDiscardChoice choice = activeChoice();
        assertThat(choice).isNotNull();
        assertThat(choice.remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Resolving against an empty hand does nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new NogginWhack()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("empty"));
    }
}
