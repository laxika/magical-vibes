package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FathomTrawlTest extends BaseCardTest {

    private void castFathomTrawl() {
        harness.setHand(player1, List.of(new FathomTrawl()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    // ===== Three nonland cards to hand, single land straight to bottom =====

    @Test
    @DisplayName("Reveals until three nonland cards, puts them in hand, single land to bottom")
    void threeNonlandToHandSingleLandToBottom() {
        Card shock1 = new Shock();
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card shock2 = new Shock();
        Card islandBottom = new Island();

        // Reveal order: Shock(nl1), Forest(land), Bears(nl2), Shock(nl3) -> stop
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(shock1, forest, bears, shock2, islandBottom));

        castFathomTrawl();

        // Only one land revealed, so it goes directly to bottom without a reorder choice
        assertThat(gd.interaction.activeInteraction()).isNull();

        assertThat(gd.playerHands.get(player1.getId())).contains(shock1, bears, shock2);
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
        Card shock1 = new Shock();
        Card island = new Island();
        Card bears = new GrizzlyBears();
        Card shock2 = new Shock();

        // Reveal order: Forest(land), Shock(nl1), Island(land), Bears(nl2), Shock(nl3) -> stop
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(forest, shock1, island, bears, shock2));

        castFathomTrawl();

        // Nonland cards are already in hand
        assertThat(gd.playerHands.get(player1.getId())).contains(shock1, bears, shock2);

        // Two lands must be ordered onto the bottom
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        assertThat(reorder).containsExactlyInAnyOrder(forest, island);

        // Choose Island closest to the top of the bottom section, then Forest
        harness.getGameService().handleLibraryCardsReordered(gd, player1,
                List.of(reorder.indexOf(island), reorder.indexOf(forest)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        // Chosen order: first chosen closest to the top of the bottom section
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island, forest);
    }

    // ===== Fewer than three nonland available (library runs out) =====

    @Test
    @DisplayName("Puts fewer than three nonland cards into hand when the library runs out")
    void libraryRunsOutBeforeThreeNonland() {
        Card shock = new Shock();
        Card forest = new Forest();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(shock, forest));

        castFathomTrawl();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(shock);
        // Single revealed land goes to the (now otherwise empty) bottom
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    // ===== No lands revealed =====

    @Test
    @DisplayName("Stops as soon as three nonland cards are revealed, leaving deeper cards untouched")
    void noLandsRevealedStopsEarly() {
        Card shock1 = new Shock();
        Card bears = new GrizzlyBears();
        Card shock2 = new Shock();
        Card islandBelow = new Island();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(shock1, bears, shock2, islandBelow));

        castFathomTrawl();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(shock1, bears, shock2);
        // Nothing was placed on the bottom; the Island stays where it was
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(islandBelow);
    }
}
