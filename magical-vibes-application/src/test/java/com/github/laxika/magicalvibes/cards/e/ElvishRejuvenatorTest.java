package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElvishRejuvenator.class, Forest.class, Shock.class})
class ElvishRejuvenatorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers one land from the top five and puts it onto the battlefield tapped")
    void offersLandAndPutsItOntoBattlefieldTapped() {
        Card forest = new Forest();
        setLibrary(new Shock(), forest, new Shock(), new Shock(), new Shock());
        castAndResolve(new ElvishRejuvenator());

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(forest.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        Permanent enteredForest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == forest)
                .findFirst()
                .orElseThrow();
        assertThat(enteredForest.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4).doesNotContain(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("ETB does nothing when the top five contain no land")
    void doesNothingWithoutLand() {
        setLibrary(new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
        castAndResolve(new ElvishRejuvenator());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard() instanceof Forest);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    void mayDeclineLandAndKeepsUnlookedCardsOnTop() {
        Card forest = new Forest();
        List<Card> topFive = List.of(forest, new Shock(), new Shock(), new Shock(), new Shock());
        Card sixth = new Shock();
        Card seventh = new Forest();
        harness.setLibrary(player1, List.of(topFive.get(0), topFive.get(1), topFive.get(2),
                topFive.get(3), topFive.get(4), sixth, seventh));
        castAndResolve(new ElvishRejuvenator());

        harness.handleMultipleCardsChosen(player1, List.of());

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library.subList(0, 2)).containsExactly(sixth, seventh);
        assertThat(library.subList(2, 7)).containsExactlyInAnyOrderElementsOf(topFive);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard() instanceof Forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void choosesOnlyOneOfMultipleLandsAndLeavesSixthCardOnTop() {
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        Card sixth = new Forest();
        List<Card> rest = List.of(firstLand, new Shock(), new Shock(), new Shock());
        setLibrary(firstLand, secondLand, rest.get(1), rest.get(2), rest.get(3), sixth);
        castAndResolve(new ElvishRejuvenator());

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(firstLand.getId(), secondLand.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(secondLand.getId()));

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library.getFirst()).isSameAs(sixth);
        assertThat(library.subList(1, 5)).containsExactlyInAnyOrderElementsOf(rest);
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(
                permanent -> permanent.getCard() == secondLand).singleElement()
                .satisfies(permanent -> assertThat(permanent.isTapped()).isTrue());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void looksAtAllAvailableCardsInShortLibrary() {
        Card forest = new Forest();
        Card shock = new Shock();
        setLibrary(shock, forest);
        castAndResolve(new ElvishRejuvenator());

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(
                permanent -> permanent.getCard() == forest).singleElement()
                .satisfies(permanent -> assertThat(permanent.isTapped()).isTrue());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryResolvesWithoutChoice() {
        setLibrary();
        castAndResolve(new ElvishRejuvenator());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Elvish Rejuvenator");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }

    private void castAndResolve(Card card) {
        harness.castFromHand(player1, card, "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
