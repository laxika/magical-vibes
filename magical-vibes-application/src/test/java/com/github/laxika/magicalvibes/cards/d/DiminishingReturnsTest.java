package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(DiminishingReturns.class)
class DiminishingReturnsTest extends BaseCardTest {

    private void castDiminishingReturns() {
        harness.setHand(player1, List.of(new DiminishingReturns()));
        harness.setHand(player2, List.of());
        fillLibrary(player1, 20);
        fillLibrary(player2, 20);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Each player may draw the full seven cards")
    void eachPlayerDrawsSeven() {
        castDiminishingReturns();

        // Active player (player1) chooses first, then the non-active player.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 7);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player2, 7);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        harness.assertInGraveyard(player1, "Diminishing Returns");
    }

    @Test
    @DisplayName("Controller exiles the top ten cards of their library")
    void controllerExilesTopTen() {
        castDiminishingReturns();
        harness.handleXValueChosen(player1, 7);
        harness.handleXValueChosen(player2, 7);

        // Only the controller exiles, and exactly ten cards (the spell itself is not exiled).
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(10);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Hand and graveyard are shuffled into the library, not lost")
    void handAndGraveyardShuffledIntoLibrary() {
        Card graveyardCard = new DiminishingReturns();
        Card handCard = new DiminishingReturns();
        harness.setHand(player1, List.of(new DiminishingReturns()));
        harness.setHand(player2, List.of(handCard));
        harness.setGraveyard(player2, List.of(graveyardCard));
        fillLibrary(player1, 20);
        fillLibrary(player2, 20);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 0);

        // The tracked cards left their original zones (shuffled into the library).
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(c -> c == graveyardCard);
        assertThat(gd.playerHands.get(player2.getId())).noneMatch(c -> c == handCard);
        assertThat(gd.playerDecks.get(player2.getId())).contains(handCard, graveyardCard);
    }

    @Test
    @DisplayName("A player may draw fewer than seven")
    void mayDrawFewerThanSeven() {
        castDiminishingReturns();

        harness.handleXValueChosen(player1, 3);
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot choose to draw more than seven")
    void cannotDrawMoreThanSeven() {
        castDiminishingReturns();

        assertThatThrownBy(() -> harness.handleXValueChosen(player1, 8))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Exiles only the cards available when the controller's library is short")
    void exilesOnlyAvailableCards() {
        harness.setHand(player1, List.of(new DiminishingReturns()));
        harness.setHand(player2, List.of());
        fillLibrary(player1, 5);
        fillLibrary(player2, 7);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(7);
    }

    @Test
    @DisplayName("The active player chooses their draw amount first")
    void activePlayerChoosesFirst() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new DiminishingReturns()));
        fillLibrary(player1, 20);
        fillLibrary(player2, 20);
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.XValueChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player2.getId());

        harness.handleXValueChosen(player2, 0);

        PendingInteraction.XValueChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(player1.getId());
        harness.handleXValueChosen(player1, 0);
    }

    private void fillLibrary(com.github.laxika.magicalvibes.model.Player player, int count) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            deck.add(new DiminishingReturns());
        }
        harness.setLibrary(player, deck);
    }
}
