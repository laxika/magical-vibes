package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Timetwister.class, GrizzlyBears.class})
class TimetwisterTest extends BaseCardTest {

    @Test
    @DisplayName("Each player shuffles hand and graveyard away and draws seven")
    void eachPlayerShufflesAndDrawsSeven() {
        Card handCard = new GrizzlyBears();
        Card graveyardCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new Timetwister()));
        harness.setHand(player2, List.of(handCard));
        gd.playerGraveyards.get(player2.getId()).add(graveyardCard);
        harness.setLibrary(player1, deckOf(20));
        harness.setLibrary(player2, deckOf(20));

        cast();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        List<Card> libraryAndHand = new ArrayList<>(gd.playerDecks.get(player2.getId()));
        libraryAndHand.addAll(gd.playerHands.get(player2.getId()));
        assertThat(libraryAndHand).contains(handCard, graveyardCard).hasSize(22);
    }

    private void cast() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private List<Card> deckOf(int count) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            deck.add(new GrizzlyBears());
        }
        return deck;
    }
}
