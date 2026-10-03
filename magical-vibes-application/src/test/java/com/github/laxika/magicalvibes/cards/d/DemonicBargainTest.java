package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SporeCrawler;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonicBargain.class, SporeCrawler.class, Plains.class, Swamp.class})
class DemonicBargainTest extends BaseCardTest {

    @Test
    void exilesTopThirteenCardsThenSearchesForACard() {
        Card bargain = new DemonicBargain();
        Card chosenCard = new Swamp();
        Card remainingCard = new Plains();
        List<Card> exiledCards = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            exiledCards.add(new SporeCrawler());
        }

        setLibrary(exiledCards, remainingCard, chosenCard);
        harness.castFromHand(player1, bargain, "{2}{B}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(exiledCards);
        assertThat(search.params().cards()).containsExactly(remainingCard, chosenCard);

        int chosenIndex = search.params().cards().indexOf(chosenCard);
        harness.handleCardChosen(player1, chosenIndex);

        assertThat(gd.playerHands.get(player1.getId())).contains(chosenCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bargain);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5, 13})
    void resolvesWithNoCardToFindWhenLibraryHasAtMostThirteenCards(int librarySize) {
        Card bargain = new DemonicBargain();
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < librarySize; i++) {
            library.add(new SporeCrawler());
        }
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, bargain, "{2}{B}");

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bargain);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mustFindTheRemainingCardAndCanChooseANonlandCard() {
        Card bargain = new DemonicBargain();
        Card chosenCard = new SporeCrawler();
        List<Card> exiledCards = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            exiledCards.add(new Swamp());
        }
        setLibrary(exiledCards, chosenCard);
        harness.castFromHand(player1, bargain, "{2}{B}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosenCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(exiledCards);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bargain);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void setLibrary(List<Card> topCards, Card... remainingCards) {
        List<Card> library = new ArrayList<>(topCards);
        library.addAll(List.of(remainingCards));
        harness.setLibrary(player1, library);
    }
}
