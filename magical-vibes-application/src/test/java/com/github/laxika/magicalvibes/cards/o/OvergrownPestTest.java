package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.f.FarmMarket;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.m.Market;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({AwakenedSkyclave.class, Forest.class, InvasionOfZendikar.class, OvergrownPest.class})
class OvergrownPestTest extends BaseCardTest {

    @Test
    @CardUsed({FarmMarket.class, Market.class, Shock.class})
    @DisplayName("ETB offers a land or double-faced card from the top five")
    void offersLandOrDoubleFacedCard() {
        Card land = new Forest();
        Card doubleFaced = new InvasionOfZendikar();
        Card split = new FarmMarket();
        List<Card> topFive = List.of(new Shock(), doubleFaced, split, land, new Shock());
        setLibrary(topFive);

        castAndResolve();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(land.getId(), doubleFaced.getId())
                .doesNotContain(split.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(doubleFaced.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(doubleFaced);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4)
                .containsExactlyInAnyOrder(topFive.get(0), topFive.get(2), topFive.get(3), topFive.get(4));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({FarmMarket.class, Market.class, Shock.class})
    @DisplayName("With no land or double-faced card, all five cards go to the bottom")
    void noEligibleCardBottomsAllFive() {
        List<Card> topFive = List.of(new Shock(), new Shock(), new FarmMarket(), new Shock(), new Shock());
        setLibrary(topFive);

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topFive);
    }

    @Test
    @DisplayName("Choosing a land preserves the unexamined library above the remaining cards")
    void choosesLandAndPreservesUnexaminedCards() {
        Card land = new Forest();
        List<Card> rest = List.of(new OvergrownPest(), new OvergrownPest(),
                new OvergrownPest(), new OvergrownPest());
        Card sixth = new InvasionOfZendikar();
        Card seventh = new Forest();
        setLibrary(List.of(rest.get(0), land, rest.get(1), rest.get(2), rest.get(3), sixth, seventh));

        castAndResolve();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(land.getId());
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(6);
        assertThat(library.subList(0, 2)).containsExactly(sixth, seventh);
        assertThat(library.subList(2, 6)).containsExactlyInAnyOrderElementsOf(rest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The player may decline even when eligible cards are available")
    void mayDeclineEligibleCards() {
        List<Card> topFive = List.of(new Forest(), new InvasionOfZendikar(),
                new OvergrownPest(), new OvergrownPest(), new OvergrownPest());
        Card sixth = new Forest();
        setLibrary(List.of(topFive.get(0), topFive.get(1), topFive.get(2),
                topFive.get(3), topFive.get(4), sixth));

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(6);
        assertThat(library.getFirst()).isSameAs(sixth);
        assertThat(library.subList(1, 6)).containsExactlyInAnyOrderElementsOf(topFive);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library with fewer than five cards still allows a selection")
    void shortLibraryAllowsSelection() {
        Card land = new Forest();
        Card other = new OvergrownPest();
        setLibrary(List.of(other, land));

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library does not require a choice or cause a loss")
    void emptyLibraryResolvesWithoutChoice() {
        setLibrary(List.of());

        castAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Overgrown Pest");
    }

    @Test
    @DisplayName("Only one eligible card may be selected")
    void cannotChooseBothLandAndDoubleFacedCard() {
        Card land = new Forest();
        Card doubleFaced = new InvasionOfZendikar();
        setLibrary(List.of(land, doubleFaced));

        castAndResolve();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(land.getId(), doubleFaced.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(doubleFaced.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(doubleFaced);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new OvergrownPest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }
}
