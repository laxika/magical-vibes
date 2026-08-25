package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CandlesOfLeng.class)
class CandlesOfLengTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the revealed card into the graveyard when its name is already there")
    void matchingNamePutsRevealedCardIntoGraveyard() {
        Card graveyardCard = createNamedCard("Shared Name");
        Card revealedCard = createNamedCard("Shared Name");
        Card nextCard = createNamedCard("Next Card");
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setLibrary(player1, List.of(revealedCard, nextCard));
        addReadyCandles();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        activateAndResolve();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(graveyardCard.getId(), revealedCard.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(nextCard.getId());
    }

    @Test
    @DisplayName("Draws the revealed card when its name is not in the graveyard")
    void nonmatchingNameDrawsRevealedCard() {
        Card graveyardCard = createNamedCard("Graveyard Card");
        Card revealedCard = createNamedCard("Revealed Card");
        Card nextCard = createNamedCard("Next Card");
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setLibrary(player1, List.of(revealedCard, nextCard));
        addReadyCandles();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        activateAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(revealedCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(graveyardCard.getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(nextCard.getId());
    }

    @Test
    @DisplayName("Does nothing when the library is empty")
    void emptyLibraryDoesNothing() {
        Card graveyardCard = createNamedCard("Graveyard Card");
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setLibrary(player1, List.of());
        addReadyCandles();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        activateAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(graveyardCard.getId());
    }

    private void addReadyCandles() {
        var candles = harness.addToBattlefieldAndReturn(player1, new CandlesOfLeng());
        candles.setSummoningSick(false);
    }

    private void activateAndResolve() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private static Card createNamedCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}");
        card.setColor(CardColor.BLUE);
        return card;
    }
}
