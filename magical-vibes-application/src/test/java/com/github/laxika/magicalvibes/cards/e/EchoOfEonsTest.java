package com.github.laxika.magicalvibes.cards.e;

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

@CardUsed({EchoOfEons.class, GrizzlyBears.class})
class EchoOfEonsTest extends BaseCardTest {

    @Test
    @DisplayName("Each player shuffles their hand and graveyard into their library, then draws seven")
    void shufflesZonesAndDrawsSeven() {
        Card handCard = new GrizzlyBears();
        Card graveyardCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new EchoOfEons()));
        harness.setHand(player2, List.of(handCard));
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setLibrary(player1, deckOf(20));
        harness.setLibrary(player2, deckOf(20));
        addNormalMana();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(20 + 2 - 7);
    }

    @Test
    @DisplayName("Flashback resolves the effect and exiles Echo of Eons")
    void flashbackResolvesAndExilesSpell() {
        EchoOfEons spell = new EchoOfEons();
        harness.setGraveyard(player1, List.of(spell));
        harness.setLibrary(player1, deckOf(20));
        harness.setLibrary(player2, deckOf(20));
        addFlashbackMana();

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        harness.assertNotInGraveyard(player1, "Echo of Eons");
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void addFlashbackMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private List<Card> deckOf(int count) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            deck.add(new GrizzlyBears());
        }
        return deck;
    }
}
