package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.n.NishobaBrawler;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FloriferousVinewall.class, Plains.class, NishobaBrawler.class, LightningStrike.class})
class FloriferousVinewallTest extends BaseCardTest {

    @Test
    void mayRevealALandFromTheTopSixIntoHand() {
        Card land = new Plains();
        List<Card> topCards = List.of(
                new LightningStrike(), new NishobaBrawler(), land,
                new LightningStrike(), new NishobaBrawler(), new LightningStrike());
        harness.setLibrary(player1, topCards);
        harness.castFromHand(player1, new FloriferousVinewall(), "{1}{G}");
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(land.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(List.of(
                        topCards.get(0), topCards.get(1), topCards.get(3),
                        topCards.get(4), topCards.get(5)));
    }

    @Test
    void decliningPutsAllTopSixCardsOnTheBottom() {
        List<Card> topCards = List.of(
                new Plains(), new LightningStrike(), new NishobaBrawler(),
                new LightningStrike(), new NishobaBrawler(), new LightningStrike());
        harness.setLibrary(player1, topCards);
        harness.castFromHand(player1, new FloriferousVinewall(), "{1}{G}");
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(topCards);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void withNoLandAmongTopSixAllCardsGoToTheBottomWithoutAChoice() {
        List<Card> topCards = List.of(
                new LightningStrike(), new NishobaBrawler(), new LightningStrike(),
                new NishobaBrawler(), new LightningStrike(), new NishobaBrawler());
        harness.setLibrary(player1, topCards);
        harness.castFromHand(player1, new FloriferousVinewall(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(topCards);
    }

    @Test
    void choosingOneOfSeveralLandsLeavesUntouchedCardsAboveTheRandomBottomPile() {
        Card chosenLand = new Plains();
        Card otherLand = new Plains();
        List<Card> topCards = List.of(chosenLand, new LightningStrike(), otherLand,
                new NishobaBrawler(), new LightningStrike(), new NishobaBrawler());
        Card seventhCard = new Plains();
        Card eighthCard = new LightningStrike();
        ArrayList<Card> library = new ArrayList<>(topCards);
        library.addAll(List.of(seventhCard, eighthCard));
        harness.setLibrary(player1, library);

        harness.castFromHand(player1, new FloriferousVinewall(), "{1}{G}");
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(chosenLand.getId(), otherLand.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosenLand.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosenLand);
        List<Card> remaining = gd.playerDecks.get(player1.getId());
        assertThat(remaining.subList(0, 2)).containsExactly(seventhCard, eighthCard);
        assertThat(remaining.subList(2, remaining.size())).containsExactlyInAnyOrderElementsOf(
                topCards.stream().filter(card -> card != chosenLand).toList());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void aLandBelowTheTopSixCannotBeChosen() {
        List<Card> topCards = List.of(new LightningStrike(), new NishobaBrawler(),
                new LightningStrike(), new NishobaBrawler(), new LightningStrike(), new NishobaBrawler());
        Card seventhCard = new Plains();
        ArrayList<Card> library = new ArrayList<>(topCards);
        library.add(seventhCard);
        harness.setLibrary(player1, library);

        harness.castFromHand(player1, new FloriferousVinewall(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        List<Card> remaining = gd.playerDecks.get(player1.getId());
        assertThat(remaining.getFirst()).isSameAs(seventhCard);
        assertThat(remaining.subList(1, remaining.size())).containsExactlyInAnyOrderElementsOf(topCards);
    }

    @Test
    void mayDeclineTheOnlyLandInAShortLibrary() {
        Card land = new Plains();
        List<Card> library = List.of(new LightningStrike(), land);
        harness.setLibrary(player1, library);

        harness.castFromHand(player1, new FloriferousVinewall(), "{1}{G}");
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayTakeTheOnlyCardWhenItIsALand() {
        Card land = new Plains();
        harness.setLibrary(player1, List.of(land));

        harness.castFromHand(player1, new FloriferousVinewall(), "{1}{G}");
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryResolvesWithoutAChoiceOrDrawing() {
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new FloriferousVinewall(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Floriferous Vinewall");
    }
}
