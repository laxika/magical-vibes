package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.ArchivistOfOghma;
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

@CardUsed({JourneyForTheElixir.class, Forest.class, JiangYanggu.class, PsychogenicProbe.class, LeoninArbiter.class,
        ArchivistOfOghma.class})
class JourneyForTheElixirTest extends BaseCardTest {

    @Test
    @DisplayName("Searches the library for a basic land and Jiang Yanggu")
    void searchesLibraryForBothCards() {
        Card forest = new Forest();
        Card jiangYanggu = new JiangYanggu();
        harness.setLibrary(player1, List.of(forest, jiangYanggu));
        castJourney();

        assertThat(search().params().cards()).containsExactly(forest);
        harness.handleCardChosen(player1, 0);
        assertThat(search().params().cards()).containsExactly(jiangYanggu);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest, jiangYanggu);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Takes the basic land from the graveyard unasked and Jiang Yanggu from the library")
    void searchesBothZones() {
        Card forest = new Forest();
        Card jiangYanggu = new JiangYanggu();
        harness.setGraveyard(player1, List.of(forest));
        harness.setLibrary(player1, List.of(jiangYanggu));
        castJourney();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(search().params().cards()).containsExactly(jiangYanggu);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(forest, jiangYanggu);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Takes both cards from the graveyard without a prompt")
    void retrievesBothCardsFromGraveyard() {
        Card forest = new Forest();
        Card jiangYanggu = new JiangYanggu();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(forest, jiangYanggu));

        castJourney();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(forest, jiangYanggu);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(forest, jiangYanggu);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Can find Jiang Yanggu when there is no basic land")
    void findsNamedCardWithoutBasicLand() {
        Card jiangYanggu = new JiangYanggu();
        harness.setLibrary(player1, List.of(jiangYanggu));

        castJourney();
        harness.handleCardChosen(player1, 0);

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
        assertThat(search().params().canFailToFind()).isTrue();
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, jiangYanggu);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A basic land in both zones offers both, and one has to be found as the graveyard holds one")
    void mustFindWhenTheGraveyardHoldsAMatch() {
        Card graveyardForest = new Forest();
        Card libraryForest = new Forest();
        harness.setLibrary(player1, List.of(libraryForest));
        harness.setGraveyard(player1, List.of(graveyardForest));
        castJourney();

        assertThat(search().params().cards()).containsExactlyInAnyOrder(libraryForest, graveyardForest);
        assertThat(search().params().canFailToFind()).isFalse();
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1)).isInstanceOf(IllegalStateException.class);

        harness.handleCardChosen(player1, search().params().cards().indexOf(graveyardForest));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(graveyardForest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryForest);
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
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Searches the library once when both cards come from it")
    void searchesTheLibraryOnceForBothCards() {
        harness.setLibrary(player1, List.of(new Forest(), new JiangYanggu()));
        harness.addToBattlefield(player2, new ArchivistOfOghma());

        castJourney();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(searchTriggers()).isEqualTo(1);
    }

    @Test
    @DisplayName("Searches the library even when both cards come from the graveyard")
    void searchesTheLibraryWhenBothCardsComeFromTheGraveyard() {
        harness.setLibrary(player1, List.of(new JourneyForTheElixir()));
        harness.setGraveyard(player1, List.of(new Forest(), new JiangYanggu()));
        harness.addToBattlefield(player2, new ArchivistOfOghma());

        castJourney();

        assertThat(gd.playersWhoSearchedLibraryThisTurn).contains(player1.getId());
        assertThat(searchTriggers()).isEqualTo(1);
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

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(jiangYanggu);
        assertThat(gd.playersWhoSearchedLibraryThisTurn).doesNotContain(player1.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Shuffles the library after taking both cards from the graveyard")
    void shufflesAfterTakingBothCardsFromGraveyard() {
        Card forest = new Forest();
        Card jiangYanggu = new JiangYanggu();
        harness.setLibrary(player1, List.of(new Forest(), new JiangYanggu()));
        harness.setGraveyard(player1, List.of(forest, jiangYanggu));
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);

        castJourney();
        harness.handleCardChosen(player1, search().params().cards().indexOf(forest));
        harness.handleCardChosen(player1, search().params().cards().indexOf(jiangYanggu));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(forest, jiangYanggu);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Still shuffles when library searching is prohibited and nothing is found")
    void shufflesWhenLibrarySearchIsPreventedAndNothingIsFound() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setGraveyard(player1, List.of());
        harness.addToBattlefield(player2, new LeoninArbiter());
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);

        castJourney();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    private PendingInteraction.LibrarySearch search() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private long searchTriggers() {
        return gd.stack.stream().filter(entry -> entry.getCard().getName().equals("Archivist of Oghma")).count();
    }

    private void castJourney() {
        harness.setHand(player1, List.of(new JourneyForTheElixir()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
