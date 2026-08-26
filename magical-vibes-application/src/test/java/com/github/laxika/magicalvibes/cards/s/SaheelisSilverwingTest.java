package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaheelisSilverwing.class, Island.class})
class SaheelisSilverwingTest extends BaseCardTest {

    @Test
    @DisplayName("ETB begins a private look at the top card of target opponent's library")
    void etbLooksAtTopCardOfTargetOpponentLibrary() {
        Card topCard = setTopCard(player2.getId(), new Island());
        castSilverwing(player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("looks at the top card"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains(topCard.getName()));
    }

    @Test
    @DisplayName("The looked-at card stays on top of the opponent's library")
    void cardStaysOnTop() {
        Card topCard = setTopCard(player2.getId(), new Island());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        castSilverwing(player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        List<Card> deckAfter = gd.playerDecks.get(player2.getId());
        assertThat(deckAfter).hasSize(deckSizeBefore);
        assertThat(deckAfter.getFirst().getId()).isEqualTo(topCard.getId());
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new SaheelisSilverwing()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private Card setTopCard(UUID playerId, Card card) {
        gd.playerDecks.get(playerId).addFirst(card);
        return card;
    }

    private void castSilverwing(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new SaheelisSilverwing()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, 0, targetPlayerId);
    }
}
