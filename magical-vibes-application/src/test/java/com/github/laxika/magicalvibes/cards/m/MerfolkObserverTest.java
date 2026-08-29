package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.cards.i.Island;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MerfolkObserverTest extends BaseCardTest {

    @Test
    @DisplayName("ETB begins a private look at the top card of target player's library")
    void etbLooksAtTopCard() {
        Card topCard = setTopCard(player2.getId(), new Island());
        castMerfolkObserver(player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at the top card"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(log -> log.contains(topCard.getName()));
    }

    @Test
    @DisplayName("Card stays on top of the library after the look")
    void cardStaysOnTop() {
        Card topCard = setTopCard(player2.getId(), new Island());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        castMerfolkObserver(player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        List<Card> deckAfter = gd.playerDecks.get(player2.getId());
        assertThat(deckAfter).hasSize(deckSizeBefore);
        assertThat(deckAfter.getFirst().getId()).isEqualTo(topCard.getId());
    }

    @Test
    @DisplayName("Can target self to look at own library")
    void canTargetSelf() {
        setTopCard(player1.getId(), new Island());
        castMerfolkObserver(player1.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

    @Test
    @DisplayName("Empty target library resolves without a look")
    void emptyLibrary() {
        gd.playerDecks.get(player2.getId()).clear();
        castMerfolkObserver(player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("library is empty"));
    }

    private Card setTopCard(UUID playerId, Card card) {
        List<Card> deck = gd.playerDecks.get(playerId);
        deck.addFirst(card);
        return card;
    }

    private void castMerfolkObserver(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new MerfolkObserver()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.getGameService().playCard(gd, player1, 0, 0, targetPlayerId, null);
    }
}
