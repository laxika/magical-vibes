package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SharedSummonsTest extends BaseCardTest {

    @Test
    @DisplayName("Offers up to two creature cards with different names")
    void offersDistinctCreatureCards() {
        GrizzlyBears bears = new GrizzlyBears();
        GrizzlyBears duplicateBears = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        setLibrary(bears, duplicateBears, elves, new Shock());
        castSharedSummons();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = activeSearch();
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyInAnyOrder(bears, duplicateBears, elves);
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().requireDifferentNames()).isTrue();
    }

    @Test
    @DisplayName("Puts two different-named creatures into hand")
    void searchesForTwoDifferentNames() {
        GrizzlyBears bears = new GrizzlyBears();
        GrizzlyBears duplicateBears = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        setLibrary(bears, duplicateBears, elves);
        castSharedSummons();

        harness.passBothPriorities();
        chooseFromLibrary(bears);

        assertThat(activeSearch().params().cards()).containsExactly(elves);
        chooseFromLibrary(elves);

        assertThat(gd.playerHands.get(player1.getId())).contains(bears, elves);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(duplicateBears);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May choose only one creature")
    void mayChooseOnlyOneCreature() {
        GrizzlyBears bears = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        setLibrary(bears, elves);
        castSharedSummons();

        harness.passBothPriorities();
        chooseFromLibrary(bears);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(elves);
        assertThat(gd.playerDecks.get(player1.getId())).contains(elves);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castSharedSummons() {
        harness.setHand(player1, List.of(new SharedSummons()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 5);
        harness.castInstant(player1, 0);
    }

    private void setLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private void chooseFromLibrary(Card card) {
        List<Card> offeredCards = activeSearch().params().cards();
        int index = offeredCards.indexOf(card);
        assertThat(index).isGreaterThanOrEqualTo(0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }
}
