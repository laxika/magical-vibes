package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FracturedSanity.class})
class FracturedSanityTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent mills fourteen cards")
    void eachOpponentMillsFourteenCards() {
        harness.setLibrary(player1, libraryOfSize(20));
        harness.setLibrary(player2, libraryOfSize(20));
        harness.setHand(player1, List.of(new FracturedSanity()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(20);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(14);
    }

    @Test
    @DisplayName("Cycling mills each opponent four cards and draws a card")
    void cyclingMillsEachOpponentAndDraws() {
        harness.setLibrary(player1, libraryOfSize(1));
        harness.setLibrary(player2, libraryOfSize(10));
        harness.setHand(player1, List.of(new FracturedSanity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        harness.assertInHand(player1, "Fractured Sanity");
        harness.assertInGraveyard(player1, "Fractured Sanity");
    }

    @Test
    @DisplayName("The cycling mill trigger resolves before the draw")
    void cyclingMillResolvesBeforeDraw() {
        FracturedSanity cycledCard = new FracturedSanity();
        FracturedSanity drawnCard = new FracturedSanity();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLibrary(player2, List.of(new FracturedSanity(), new FracturedSanity()));
        harness.setHand(player1, List.of(cycledCard));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cycledCard);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting mills only the cards remaining in a short library")
    void castingMillsShortLibrary() {
        harness.setLibrary(player2, List.of(new FracturedSanity(), new FracturedSanity()));
        harness.setHand(player1, List.of(new FracturedSanity()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Fractured Sanity");
    }

    @Test
    @DisplayName("Cycling still draws when the opponent's library is empty")
    void cyclingDrawsWithEmptyOpposingLibrary() {
        FracturedSanity drawnCard = new FracturedSanity();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new FracturedSanity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private List<Card> libraryOfSize(int size) {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            library.add(new FracturedSanity());
        }
        return library;
    }
}
