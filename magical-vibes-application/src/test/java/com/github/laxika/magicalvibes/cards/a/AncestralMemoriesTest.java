package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AncestralMemoriesTest extends BaseCardTest {

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.BLUE, 3);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Resolving enters library reveal choice state")
    void resolvingEntersRevealChoiceState() {
        setupTopCards(sevenCards());

        harness.setHand(player1, List.of(new AncestralMemories()));
        addMana(player1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
    }

    @Test
    @DisplayName("Choosing two cards puts them into hand and the other five into the graveyard")
    void choosingTwoPutsTwoInHandFiveInGraveyard() {
        List<Card> cards = sevenCards();
        setupTopCards(cards);

        harness.setHand(player1, List.of(new AncestralMemories()));
        addMana(player1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Card chosen0 = cards.get(0);
        Card chosen1 = cards.get(3);
        harness.handleMultipleCardsChosen(player1, List.of(chosen0.getId(), chosen1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen0, chosen1);

        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        // The five unchosen cards land in the graveyard (plus Ancestral Memories itself)
        for (Card c : cards) {
            if (!c.getId().equals(chosen0.getId()) && !c.getId().equals(chosen1.getId())) {
                assertThat(graveyard).contains(c);
            }
        }
        assertThat(graveyard).noneMatch(c -> c.getId().equals(chosen0.getId()));
        assertThat(graveyard).noneMatch(c -> c.getId().equals(chosen1.getId()));
    }

    @Test
    @DisplayName("Choosing clears the awaiting state and empties the revealed cards from the library")
    void choosingClearsStateAndLibrary() {
        List<Card> cards = sevenCards();
        setupTopCards(cards);

        harness.setHand(player1, List.of(new AncestralMemories()));
        addMana(player1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of(cards.get(0).getId(), cards.get(1).getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With only two cards in library, both go directly to hand (no choice needed)")
    void twoCardsInLibraryBothGoToHand() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        Card cardA = new GrizzlyBears();
        Card cardB = new Shock();
        gd.playerDecks.get(player1.getId()).add(cardA);
        gd.playerDecks.get(player1.getId()).add(cardB);

        harness.setHand(player1, List.of(new AncestralMemories()));
        addMana(player1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(cardA, cardB);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With empty library, nothing is drawn")
    void emptyLibrary() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();

        harness.setHand(player1, List.of(new AncestralMemories()));
        addMana(player1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private List<Card> sevenCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            cards.add(i % 2 == 0 ? new GrizzlyBears() : new Shock());
        }
        return cards;
    }

    private void setupTopCards(List<Card> cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }
}
