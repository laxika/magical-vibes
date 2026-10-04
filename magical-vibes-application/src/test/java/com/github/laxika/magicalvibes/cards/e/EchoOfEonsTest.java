package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

        harness.castAndResolveSorcery(player1, 0, 0);

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

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        harness.assertNotInGraveyard(player1, "Echo of Eons");
    }

    @Test
    @DisplayName("Both players rebuild empty libraries from their hands and graveyards before drawing")
    void rebuildsEmptyLibrariesBeforeDrawing() {
        EchoOfEons spell = new EchoOfEons();
        List<Card> firstCards = new ArrayList<>();
        List<Card> secondCards = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            firstCards.add(new EchoOfEons());
            secondCards.add(new EchoOfEons());
        }
        List<Card> firstHand = new ArrayList<>(firstCards.subList(0, 3));
        firstHand.add(0, spell);
        harness.setHand(player1, firstHand);
        harness.setGraveyard(player1, firstCards.subList(3, 7));
        harness.setHand(player2, secondCards.subList(0, 2));
        harness.setGraveyard(player2, secondCards.subList(2, 7));
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        addNormalMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(firstCards);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrderElementsOf(secondCards);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Echo of Eons can be flashed back after resolving from hand")
    void normalCastThenFlashback() {
        EchoOfEons spell = new EchoOfEons();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, deckOf(20));
        harness.setLibrary(player2, deckOf(20));
        addNormalMana();
        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);

        addFlashbackMana();
        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7).doesNotContain(spell);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(13).doesNotContain(spell);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(13);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
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
