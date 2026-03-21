package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TimeReversalTest extends BaseCardTest {

    // ===== Hand and graveyard shuffle =====

    @Test
    @DisplayName("Each player draws 7 cards after shuffling hand and graveyard into library")
    void eachPlayerDrawsSeven() {
        harness.setHand(player1, List.of(new TimeReversal()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        fillDeck(player1, 20);
        fillDeck(player2, 20);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Each player draws exactly 7, regardless of prior hand size
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Hand cards are shuffled into library, not discarded")
    void handCardsGoIntoLibrary() {
        Card trackedCard = new GrizzlyBears();
        harness.setHand(player2, List.of(trackedCard));
        harness.setHand(player1, List.of(new TimeReversal()));

        fillDeck(player1, 20);
        fillDeck(player2, 20);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // The tracked card should not be in the graveyard (it was shuffled into library)
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c == trackedCard);
    }

    @Test
    @DisplayName("Graveyard cards are shuffled into library")
    void graveyardCardsGoIntoLibrary() {
        harness.setHand(player1, List.of(new TimeReversal()));
        harness.setHand(player2, List.of());

        // Put some cards in player2's graveyard
        Card graveyardCard1 = new GrizzlyBears();
        Card graveyardCard2 = new GrizzlyBears();
        gd.playerGraveyards.get(player2.getId()).add(graveyardCard1);
        gd.playerGraveyards.get(player2.getId()).add(graveyardCard2);

        fillDeck(player1, 20);
        fillDeck(player2, 20);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Graveyard should be empty after the shuffle
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Player with empty hand and graveyard still draws 7")
    void emptyHandAndGraveyardStillDrawsSeven() {
        harness.setHand(player1, List.of(new TimeReversal()));
        harness.setHand(player2, List.of());

        fillDeck(player1, 20);
        fillDeck(player2, 20);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Even with empty hand and graveyard, player still draws 7
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
    }

    // ===== Exile =====

    @Test
    @DisplayName("Time Reversal is exiled after resolution, not in graveyard")
    void spellIsExiledAfterResolution() {
        harness.setHand(player1, List.of(new TimeReversal()));
        harness.setHand(player2, List.of());

        fillDeck(player1, 20);
        fillDeck(player2, 20);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Time Reversal should be exiled, not in graveyard
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Time Reversal"));
        harness.assertNotInGraveyard(player1, "Time Reversal");
    }

    // ===== Library size =====

    @Test
    @DisplayName("Library contains shuffled hand and graveyard cards minus 7 drawn")
    void librarySizeIsCorrect() {
        harness.setHand(player1, List.of(new TimeReversal()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        Card gy1 = new GrizzlyBears();
        Card gy2 = new GrizzlyBears();
        gd.playerGraveyards.get(player2.getId()).add(gy1);
        gd.playerGraveyards.get(player2.getId()).add(gy2);

        fillDeck(player1, 20);
        fillDeck(player2, 10);

        int deckBefore = gd.playerDecks.get(player2.getId()).size();
        int handSize = 3;
        int graveyardSize = 2;

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // All hand + graveyard shuffled into library, then 7 drawn
        int expectedDeck = deckBefore + handSize + graveyardSize - 7;
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(expectedDeck);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
    }

    // ===== Helpers =====

    private void fillDeck(com.github.laxika.magicalvibes.model.Player player, int count) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        if (deck == null) {
            deck = new ArrayList<>();
            gd.playerDecks.put(player.getId(), deck);
        }
        for (int i = 0; i < count; i++) {
            deck.add(new GrizzlyBears());
        }
    }
}
