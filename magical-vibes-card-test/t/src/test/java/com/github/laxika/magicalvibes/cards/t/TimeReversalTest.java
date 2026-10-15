package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimeReversal.class, RuneclawBear.class})
class TimeReversalTest extends BaseCardTest {

    @Test
    @DisplayName("Each player draws 7 cards after shuffling hand and graveyard into library")
    void eachPlayerDrawsSeven() {
        harness.setHand(player2, List.of(new RuneclawBear(), new RuneclawBear()));

        fillDeck(player1, 20);
        fillDeck(player2, 20);

        harness.castFromHand(player1, new TimeReversal(), "{3}{U}{U}");
        harness.passBothPriorities();

        // Each player draws exactly 7, regardless of prior hand size
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Hand cards are shuffled into library, not discarded")
    void handCardsGoIntoLibrary() {
        Card trackedCard = new RuneclawBear();
        harness.setHand(player2, List.of(trackedCard));

        fillDeck(player1, 20);
        fillDeck(player2, 20);

        harness.castFromHand(player1, new TimeReversal(), "{3}{U}{U}");
        harness.passBothPriorities();

        // The tracked card should not be in the graveyard (it was shuffled into library)
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c == trackedCard);
    }

    @Test
    @DisplayName("Graveyard cards are shuffled into library")
    void graveyardCardsGoIntoLibrary() {
        harness.setHand(player2, List.of());

        // Put some cards in player2's graveyard
        Card graveyardCard1 = new RuneclawBear();
        Card graveyardCard2 = new RuneclawBear();
        harness.setGraveyard(player2, List.of(graveyardCard1, graveyardCard2));

        fillDeck(player1, 20);
        fillDeck(player2, 20);

        harness.castFromHand(player1, new TimeReversal(), "{3}{U}{U}");
        harness.passBothPriorities();

        // Graveyard should be empty after the shuffle
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Player with empty hand and graveyard still draws 7")
    void emptyHandAndGraveyardStillDrawsSeven() {
        harness.setHand(player2, List.of());

        fillDeck(player1, 20);
        fillDeck(player2, 20);

        harness.castFromHand(player1, new TimeReversal(), "{3}{U}{U}");
        harness.passBothPriorities();

        // Even with empty hand and graveyard, player still draws 7
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Time Reversal is exiled after resolution, not in graveyard")
    void spellIsExiledAfterResolution() {
        harness.setHand(player2, List.of());

        fillDeck(player1, 20);
        fillDeck(player2, 20);

        harness.castFromHand(player1, new TimeReversal(), "{3}{U}{U}");
        harness.passBothPriorities();

        // Time Reversal should be exiled, not in graveyard
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Time Reversal"));
        harness.assertNotInGraveyard(player1, "Time Reversal");
    }

    @Test
    @DisplayName("Library contains shuffled hand and graveyard cards minus 7 drawn")
    void librarySizeIsCorrect() {
        harness.setHand(player2, List.of(new RuneclawBear(), new RuneclawBear(), new RuneclawBear()));

        Card gy1 = new RuneclawBear();
        Card gy2 = new RuneclawBear();
        harness.setGraveyard(player2, List.of(gy1, gy2));

        fillDeck(player1, 20);
        fillDeck(player2, 10);

        int deckBefore = gd.playerDecks.get(player2.getId()).size();
        int handSize = 3;
        int graveyardSize = 2;

        harness.castFromHand(player1, new TimeReversal(), "{3}{U}{U}");
        harness.passBothPriorities();

        // All hand + graveyard shuffled into library, then 7 drawn
        int expectedDeck = deckBefore + handSize + graveyardSize - 7;
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(expectedDeck);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Both players decking out during resolution lose simultaneously")
    void bothPlayersWithTooFewCardsDrawTheGame() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new RuneclawBear()));
        harness.setLibrary(player2, List.of(new RuneclawBear()));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        TimeReversal spell = new TimeReversal();
        harness.castFromHand(player1, spell, "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isNull();
    }

    @Test
    @DisplayName("An empty library draws from recycled hand and graveyard cards")
    void recycledCardsAreAvailableForTheSevenDraws() {
        List<Card> hand = List.of(new RuneclawBear(), new RuneclawBear(), new RuneclawBear());
        List<Card> graveyard = List.of(new RuneclawBear(), new RuneclawBear(),
                new RuneclawBear(), new RuneclawBear());
        harness.setHand(player2, hand);
        harness.setGraveyard(player2, graveyard);
        harness.setLibrary(player2, List.of());
        fillDeck(player1, 20);

        harness.castFromHand(player1, new TimeReversal(), "{3}{U}{U}");
        harness.passBothPriorities();

        List<Card> recycled = new ArrayList<>(hand);
        recycled.addAll(graveyard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrderElementsOf(recycled);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    private void fillDeck(com.github.laxika.magicalvibes.model.Player player, int count) {
        List<Card> deck = new ArrayList<>(gd.playerDecks.getOrDefault(player.getId(), List.of()));
        for (int i = 0; i < count; i++) {
            deck.add(new RuneclawBear());
        }
        harness.setLibrary(player, deck);
    }
}
