package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RomanticRendezvous.class, Forest.class, Mountain.class})
class RomanticRendezvousTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card draws two cards")
    void discardsThenDrawsTwo() {
        Card firstDraw = new Forest();
        Card secondDraw = new Mountain();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new RomanticRendezvous(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("With no card to discard, the spell still draws two cards")
    void noCardToDiscardStillDrawsTwo() {
        Card firstDraw = new Forest();
        Card secondDraw = new Mountain();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new RomanticRendezvous()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choose one existing card to discard before drawing, leaving the opponent unaffected")
    void choosesDiscardBeforeDrawing() {
        Card keptCard = new Forest();
        Card discardedCard = new Mountain();
        Card firstDraw = new Forest();
        Card secondDraw = new Mountain();
        Card opponentCard = new Mountain();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new RomanticRendezvous(), keptCard, discardedCard));
        harness.setHand(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptCard, discardedCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstDraw, secondDraw);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptCard, firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Romantic Rendezvous");
    }
}
