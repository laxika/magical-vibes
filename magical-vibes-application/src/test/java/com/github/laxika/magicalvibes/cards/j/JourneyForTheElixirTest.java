package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JourneyForTheElixir.class, Forest.class, JiangYanggu.class})
class JourneyForTheElixirTest extends BaseCardTest {

    @Test
    @DisplayName("Searches the library for a basic land and Jiang Yanggu")
    void searchesLibraryForBothCards() {
        Card forest = new Forest();
        Card jiangYanggu = new JiangYanggu();
        harness.setLibrary(player1, List.of(forest, jiangYanggu));
        castJourney();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice basicLandSearch =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(basicLandSearch.validCardIds()).containsExactly(forest.getId());
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        PendingInteraction.SearchLibraryAndOrGraveyardChoice jiangYangguSearch =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(jiangYangguSearch.validCardIds()).containsExactly(jiangYanggu.getId());
        harness.handleMultipleCardsChosen(player1, List.of(jiangYanggu.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(forest, jiangYanggu);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can take the basic land from the graveyard and Jiang Yanggu from the library")
    void searchesBothZones() {
        Card forest = new Forest();
        Card jiangYanggu = new JiangYanggu();
        harness.setGraveyard(player1, List.of(forest));
        harness.setLibrary(player1, List.of(jiangYanggu));
        castJourney();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice basicLandSearch =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(basicLandSearch.validCardIds()).containsExactly(forest.getId());
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        PendingInteraction.SearchLibraryAndOrGraveyardChoice jiangYangguSearch =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(jiangYangguSearch.validCardIds()).containsExactly(jiangYanggu.getId());
        harness.handleMultipleCardsChosen(player1, List.of(jiangYanggu.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(forest, jiangYanggu);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castJourney() {
        harness.setHand(player1, List.of(new JourneyForTheElixir()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
