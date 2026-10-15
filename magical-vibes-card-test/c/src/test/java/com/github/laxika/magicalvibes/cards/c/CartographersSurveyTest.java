package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CartographersSurvey.class, Forest.class, Abrade.class})
class CartographersSurveyTest extends BaseCardTest {

    @Test
    @DisplayName("Puts up to two revealed lands onto the battlefield tapped")
    void putsUpToTwoLandsOntoBattlefieldTapped() {
        Card forest1 = new Forest();
        Card forest2 = new Forest();
        setLibrary(forest1, new Abrade(), forest2, new Abrade(), new Abrade(), new Abrade(), new Abrade());

        castAndResolve();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(forest1.getId(), forest2.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.randomRemainingToBottom()).isTrue();
        assertThat(choice.selectedToBattlefieldTapped()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(forest1.getId(), forest2.getId()));

        assertThat(permanentFor(forest1).isTapped()).isTrue();
        assertThat(permanentFor(forest2).isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May put only one revealed land onto the battlefield")
    void mayPutOnlyOneLand() {
        Card forest = new Forest();
        setLibrary(forest, new Abrade(), new Abrade(), new Abrade(), new Abrade(), new Abrade(), new Abrade());

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(permanentFor(forest).isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Puts all seven cards on the bottom when no land is revealed")
    void noLandLeavesLibraryIntact() {
        setLibrary(new Abrade(), new Abrade(), new Abrade(), new Abrade(), new Abrade(), new Abrade(), new Abrade());

        castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayDeclineAllLands() {
        Card forest1 = new Forest();
        Card forest2 = new Forest();
        Card nonland = new Abrade();
        setLibrary(forest1, forest2, nonland);

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(forest1, forest2, nonland);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void looksOnlyAtTopSevenAndBottomsUnchosenLands() {
        Card forest1 = new Forest();
        Card forest2 = new Forest();
        Card forest3 = new Forest();
        Card belowTopSeven = new Forest();
        List<Card> topSeven = List.of(forest1, forest2, forest3,
                new Abrade(), new Abrade(), new Abrade(), new Abrade());
        harness.setLibrary(player1, java.util.stream.Stream.concat(
                topSeven.stream(), java.util.stream.Stream.of(belowTopSeven)).toList());

        castAndResolve();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(
                forest1.getId(), forest2.getId(), forest3.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, List.of(forest1.getId(), forest2.getId()));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(belowTopSeven);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrderElementsOf(topSeven.subList(2, 7));
        assertThat(permanentFor(forest1).isTapped()).isTrue();
        assertThat(permanentFor(forest2).isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canChooseOneOfTwoLandsInShortLibrary() {
        Card chosen = new Forest();
        Card unchosen = new Forest();
        setLibrary(chosen, unchosen);

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(permanentFor(chosen).isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unchosen);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesWithEmptyLibrary() {
        setLibrary();

        castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Cartographer's Survey");
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new CartographersSurvey()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }

    private Permanent permanentFor(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst()
                .orElseThrow();
    }
}
