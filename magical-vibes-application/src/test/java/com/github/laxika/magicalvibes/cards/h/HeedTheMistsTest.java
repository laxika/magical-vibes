package com.github.laxika.magicalvibes.cards.h;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HeedTheMistsTest extends BaseCardTest {

    @Test
    @DisplayName("Mills the top card and draws cards equal to its mana value")
    void millsAndDrawsByManaValue() {
        prepare();
        setDeck(player1, new GrizzlyBears(), new Mountain(), new Mountain(), new Mountain());
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        // Grizzly Bears (mana value 2) is milled, then two cards are drawn.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2); // milled card + Heed the Mists
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize - 1 + 2);
    }

    @Test
    @DisplayName("Draws nothing when the milled card has mana value zero")
    void noDrawForZeroManaValue() {
        prepare();
        setDeck(player1, new Mountain(), new Mountain(), new Mountain());
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize - 1);
    }

    @Test
    @DisplayName("Does nothing when the library is empty")
    void emptyLibraryDoesNothing() {
        prepare();
        setDeck(player1);
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize - 1);
    }

    private void prepare() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new HeedTheMists()));
        harness.addMana(player1, ManaColor.BLUE, 5);
    }

    private void setDeck(Player player, Card... cards) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
