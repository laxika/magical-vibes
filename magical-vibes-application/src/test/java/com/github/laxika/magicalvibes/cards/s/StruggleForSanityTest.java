package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StruggleForSanityTest extends BaseCardTest {

    private PendingInteraction.AlternatingHandExileChoice activeChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.AlternatingHandExileChoice.class);
    }

    private void castStruggle() {
        harness.setHand(player1, List.of(new StruggleForSanity()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The targeted player picks first")
    void targetPicksFirst() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));

        castStruggle();

        PendingInteraction.AlternatingHandExileChoice choice = activeChoice();
        assertThat(choice).isNotNull();
        assertThat(choice.decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(choice.validIndices()).containsExactly(0, 1);
    }

    @Test
    @DisplayName("Picks alternate between the target and the controller")
    void picksAlternate() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new Peek(), new HornedTurtle(), new Forest())));

        castStruggle();

        harness.handleCardChosen(player2, 0);
        assertThat(activeChoice().decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(activeChoice().validIndices()).containsExactly(0, 1, 2);

        harness.handleCardChosen(player1, 0);
        assertThat(activeChoice().decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(activeChoice().validIndices()).containsExactly(0, 1);
    }

    @Test
    @DisplayName("Target keeps the cards they exiled; the controller's picks hit the graveyard")
    void keptAndBinnedPiles() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new Peek(), new HornedTurtle(), new Forest())));

        castStruggle();

        // player2 exiles Grizzly Bears, player1 exiles Peek,
        // player2 exiles Horned Turtle, player1 exiles Forest.
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Horned Turtle");
        harness.assertInGraveyard(player2, "Peek");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("An odd hand size leaves the last card to the target, who keeps it")
    void oddHandSizeLastPickIsTheTargets() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new Peek(), new HornedTurtle())));

        castStruggle();

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Horned Turtle");
        harness.assertInGraveyard(player2, "Peek");
    }

    @Test
    @DisplayName("Cards sit in exile between picks")
    void cardsAreInExileBetweenPicks() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));

        castStruggle();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.exiledCards).hasSize(1);
        assertThat(gd.exiledCards.getFirst().card().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Resolving against an empty hand does nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, new ArrayList<>());

        castStruggle();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("empty"));
    }

    @Test
    @DisplayName("The wrong player cannot answer the current pick")
    void wrongPlayerCannotChoose() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));

        castStruggle();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    @Test
    @DisplayName("An out-of-range card index is rejected")
    void invalidIndexRejected() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));

        castStruggle();

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 7))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }
}
