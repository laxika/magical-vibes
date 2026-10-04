package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CennsHeir;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FathomTrawl.class, CennsHeir.class, Forest.class, Island.class})
class FathomTrawlTest extends BaseCardTest {

    private void castFathomTrawl() {
        harness.castFromHand(player1, new FathomTrawl(), "{3}{U}{U}");
        harness.passBothPriorities();
    }

    // ===== Three nonland cards to hand, single land straight to bottom =====

    @Test
    @DisplayName("Reveals until three nonland cards, puts them in hand, single land to bottom")
    void threeNonlandToHandSingleLandToBottom() {
        Card firstNonland = new CennsHeir();
        Card forest = new Forest();
        Card secondNonland = new CennsHeir();
        Card thirdNonland = new CennsHeir();
        Card islandBottom = new Island();

        // Reveal order: nonland, Forest(land), nonland, nonland -> stop
        harness.setLibrary(player1, List.of(firstNonland, forest, secondNonland, thirdNonland, islandBottom));

        castFathomTrawl();

        // Only one land revealed, so it goes directly to bottom without a reorder choice
        assertThat(gd.interaction.activeInteraction()).isNull();

        assertThat(gd.playerHands.get(player1.getId())).contains(firstNonland, secondNonland, thirdNonland);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);

        // Library: the untouched Island remains on top, the revealed Forest is now on the bottom
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).containsExactly(islandBottom, forest);
    }

    // ===== Multiple lands revealed -> reorder-to-bottom interaction =====

    @Test
    @DisplayName("When two lands are revealed, controller reorders them onto the bottom")
    void twoLandsTriggerReorderInteraction() {
        Card forest = new Forest();
        Card firstNonland = new CennsHeir();
        Card island = new Island();
        Card secondNonland = new CennsHeir();
        Card thirdNonland = new CennsHeir();

        // Reveal order: Forest(land), nonland, Island(land), nonland, nonland -> stop
        harness.setLibrary(player1, List.of(forest, firstNonland, island, secondNonland, thirdNonland));

        castFathomTrawl();

        // Nonland cards are already in hand
        assertThat(gd.playerHands.get(player1.getId())).contains(firstNonland, secondNonland, thirdNonland);

        // Two lands must be ordered onto the bottom
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        assertThat(reorder).containsExactlyInAnyOrder(forest, island);

        // Choose Island closest to the top of the bottom section, then Forest
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(reorder.indexOf(island), reorder.indexOf(forest))));

        assertThat(gd.interaction.activeInteraction()).isNull();
        // Chosen order: first chosen closest to the top of the bottom section
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island, forest);
    }

    // ===== Fewer than three nonland available (library runs out) =====

    @Test
    @DisplayName("Puts fewer than three nonland cards into hand when the library runs out")
    void libraryRunsOutBeforeThreeNonland() {
        Card nonland = new CennsHeir();
        Card forest = new Forest();

        harness.setLibrary(player1, List.of(nonland, forest));

        castFathomTrawl();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(nonland);
        // Single revealed land goes to the (now otherwise empty) bottom
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Reorders all revealed lands when the library runs out before three nonland cards")
    void libraryRunsOutWithMultipleLands() {
        Card nonland = new CennsHeir();
        Card forest = new Forest();
        Card island = new Island();
        Card secondForest = new Forest();

        harness.setLibrary(player1, List.of(nonland, forest, island, secondForest));

        castFathomTrawl();

        assertThat(gd.playerHands.get(player1.getId())).contains(nonland);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        assertThat(reorder).containsExactlyInAnyOrder(forest, island, secondForest);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(
                        reorder.indexOf(secondForest), reorder.indexOf(island), reorder.indexOf(forest))));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondForest, island, forest);
    }

    @Test
    @DisplayName("Reveals nothing when the library is empty")
    void emptyLibraryRevealsNothing() {
        harness.setLibrary(player1, List.of());

        castFathomTrawl();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    // ===== No lands revealed =====

    @Test
    @DisplayName("Stops as soon as three nonland cards are revealed, leaving deeper cards untouched")
    void noLandsRevealedStopsEarly() {
        Card firstNonland = new CennsHeir();
        Card secondNonland = new CennsHeir();
        Card thirdNonland = new CennsHeir();
        Card islandBelow = new Island();

        harness.setLibrary(player1, List.of(firstNonland, secondNonland, thirdNonland, islandBelow));

        castFathomTrawl();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(firstNonland, secondNonland, thirdNonland);
        // Nothing was placed on the bottom; the Island stays where it was
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(islandBelow);
    }

    @Test
    @DisplayName("An all-land library is revealed and reordered without putting cards in hand")
    void allLandLibraryIsReorderedWithoutDrawing() {
        Card forest = new Forest();
        Card island = new Island();
        harness.setLibrary(player1, List.of(forest, island));

        castFathomTrawl();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        assertThat(reorder).containsExactlyInAnyOrder(forest, island);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(reorder.indexOf(island), reorder.indexOf(forest))));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island, forest);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Reordered lands go below every unrevealed card, including a fourth nonland")
    void reorderedLandsStayBelowUnrevealedCards() {
        Card forest = new Forest();
        Card island = new Island();
        Card firstNonland = new CennsHeir();
        Card secondNonland = new CennsHeir();
        Card thirdNonland = new CennsHeir();
        Card fourthNonland = new CennsHeir();
        Card untouchedLand = new Forest();
        harness.setLibrary(player1, List.of(forest, firstNonland, island, secondNonland,
                thirdNonland, fourthNonland, untouchedLand));

        castFathomTrawl();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(firstNonland, secondNonland, thirdNonland);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourthNonland, untouchedLand);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        assertThat(reorder).containsExactlyInAnyOrder(forest, island);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(reorder.indexOf(island), reorder.indexOf(forest))));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(fourthNonland, untouchedLand, island, forest);
    }
}
