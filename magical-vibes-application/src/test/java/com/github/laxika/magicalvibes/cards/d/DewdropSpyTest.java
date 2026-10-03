package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.m.MothdustChangeling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DewdropSpy.class, MothdustChangeling.class})
class DewdropSpyTest extends BaseCardTest {

    @Test
    @DisplayName("ETB begins a private look at the top card of target player's library")
    void etbLooksAtTopCard() {
        Card topCard = setTopCard(player2.getId(), new MothdustChangeling());
        castDewdropSpy(player2.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at the top card"));
        // The card's identity is never broadcast publicly for a private look.
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(log -> log.contains(topCard.getName()));
    }

    @Test
    @DisplayName("Card stays on top of the library after the look")
    void cardStaysOnTop() {
        Card topCard = setTopCard(player2.getId(), new MothdustChangeling());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        castDewdropSpy(player2.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Close the private look without moving anything.
        harness.handleCardChosen(player1, -1);

        List<Card> deckAfter = gd.playerDecks.get(player2.getId());
        assertThat(deckAfter).hasSize(deckSizeBefore);
        assertThat(deckAfter.getFirst().getId()).isEqualTo(topCard.getId());
    }

    @Test
    @DisplayName("Can target self to look at own library")
    void canTargetSelf() {
        setTopCard(player1.getId(), new MothdustChangeling());
        castDewdropSpy(player1.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at the top card"));
    }

    @Test
    @DisplayName("Empty target library resolves without a look")
    void emptyLibrary() {
        gd.playerDecks.get(player2.getId()).clear();
        castDewdropSpy(player2.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("library is empty"));
    }

    @Test
    @DisplayName("Looks at the current top card when the trigger resolves and preserves library order")
    void looksAtTopCardAtResolution() {
        Card originalTop = new MothdustChangeling();
        Card newTop = new DewdropSpy();
        harness.setLibrary(player2, List.of(originalTop));
        castDewdropSpy(player2.getId());

        harness.passBothPriorities();
        harness.setLibrary(player2, List.of(newTop, originalTop));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch look =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(look).isNotNull();
        assertThat(look.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(look.params().cards()).containsExactly(newTop);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(newTop, originalTop);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Card setTopCard(UUID playerId, Card card) {
        List<Card> deck = gd.playerDecks.get(playerId);
        deck.addFirst(card);
        return card;
    }

    private void castDewdropSpy(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new DewdropSpy()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, targetPlayerId);
    }
}
