package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.cards.i.Island;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerfolkObserver.class, Island.class})
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
        harness.handleCardChosen(player1, -1);

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

    @Test
    @DisplayName("Only the controller looks at exactly the top card, and selecting it preserves library order")
    void selectingLookedAtCardPreservesLibraryOrder() {
        Card topCard = new Island();
        Card secondCard = new MerfolkObserver();
        Card thirdCard = new Island();
        List<Card> library = List.of(topCard, secondCard, thirdCard);
        harness.setLibrary(player2, library);
        castMerfolkObserver(player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch look =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(look).isNotNull();
        assertThat(look.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(look.params().cards()).containsExactly(topCard);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains(topCard.getName()));
    }

    @Test
    @DisplayName("Acknowledging the look without selecting preserves every card in order")
    void acknowledgingLookPreservesLibraryOrder() {
        List<Card> library = List.of(new Island(), new MerfolkObserver(), new Island());
        harness.setLibrary(player1, library);
        castMerfolkObserver(player1.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Card setTopCard(UUID playerId, Card card) {
        List<Card> deck = gd.playerDecks.get(playerId);
        deck.addFirst(card);
        return card;
    }

    private void castMerfolkObserver(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new MerfolkObserver()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, targetPlayerId);
    }
}
