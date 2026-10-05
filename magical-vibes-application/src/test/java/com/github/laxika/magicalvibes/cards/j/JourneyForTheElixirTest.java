package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.cards.l.LeoninArbiter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JourneyForTheElixir.class, Forest.class, JiangYanggu.class, PsychogenicProbe.class, LeoninArbiter.class})
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

    @Test
    @DisplayName("Can retrieve both cards from the graveyard")
    void retrievesBothCardsFromGraveyard() {
        Card forest = new Forest();
        Card jiangYanggu = new JiangYanggu();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(forest, jiangYanggu));

        castJourney();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(jiangYanggu.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(forest, jiangYanggu);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .doesNotContain(forest, jiangYanggu);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Can find Jiang Yanggu when there is no basic land")
    void findsNamedCardWithoutBasicLand() {
        Card jiangYanggu = new JiangYanggu();
        harness.setLibrary(player1, List.of(jiangYanggu));

        castJourney();
        harness.handleMultipleCardsChosen(player1, List.of(jiangYanggu.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(jiangYanggu);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Resolves without finding cards when neither zone has a match")
    void resolvesWithoutMatches() {
        Card otherJourney = new JourneyForTheElixir();
        harness.setLibrary(player1, List.of(otherJourney));
        harness.setGraveyard(player1, List.of());

        castJourney();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherJourney);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Journey for the Elixir");
    }

    @Test
    @DisplayName("May fail to find either matching card in the library")
    void mayFailToFindLibraryCards() {
        Card forest = new Forest();
        Card jiangYanggu = new JiangYanggu();
        harness.setLibrary(player1, List.of(forest, jiangYanggu));

        castJourney();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, jiangYanggu);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cannot fail to find a matching basic land available only in the graveyard")
    void mustFindMatchingGraveyardCard() {
        Card forest = new Forest();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(forest));
        castJourney();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(forest);
    }

    @Test
    @DisplayName("Shuffles once after finding both cards")
    void shufflesLibraryOnlyOnce() {
        Card forest = new Forest();
        Card jiangYanggu = new JiangYanggu();
        harness.setLibrary(player1, List.of(forest, jiangYanggu));
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);

        castJourney();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(jiangYanggu.getId()));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Still shuffles and searches the graveyard when library searching is prohibited")
    void shufflesWhenLibrarySearchIsPrevented() {
        Card forest = new Forest();
        Card jiangYanggu = new JiangYanggu();
        harness.setLibrary(player1, List.of(jiangYanggu));
        harness.setGraveyard(player1, List.of(forest));
        harness.addToBattlefield(player2, new LeoninArbiter());
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);

        castJourney();
        PendingInteraction.SearchLibraryAndOrGraveyardChoice search =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(search.validCardIds()).containsExactly(forest.getId());
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(jiangYanggu);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        assertThat(gd.stack).isEmpty();
    }

    private void castJourney() {
        harness.setHand(player1, List.of(new JourneyForTheElixir()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
