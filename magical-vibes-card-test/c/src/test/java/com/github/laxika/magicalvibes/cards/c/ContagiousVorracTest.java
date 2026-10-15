package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ContagiousVorrac.class, Forest.class})
class ContagiousVorracTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers a land from the top four and taking it does not proliferate")
    void offersLandAndTakingItDoesNotProliferate() {
        Permanent vorrac = addCounteredVorrac();
        Forest forest = new Forest();
        List<Card> topCards = List.of(forest, new ContagiousVorrac(), new ContagiousVorrac(), new ContagiousVorrac());
        harness.setLibrary(player1, topCards);

        castAndResolve();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(forest.getId());

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(vorrac.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the land puts the top four on the bottom and proliferates")
    void decliningLandProliferates() {
        Permanent vorrac = addCounteredVorrac();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new ContagiousVorrac(), new ContagiousVorrac(), new ContagiousVorrac()));

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(vorrac.getId()));

        assertThat(vorrac.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no land among the top four, the cards bottom immediately and proliferate")
    void noLandProliferatesWithoutChoice() {
        Permanent vorrac = addCounteredVorrac();
        List<Card> topCards = List.of(new ContagiousVorrac(), new ContagiousVorrac(), new ContagiousVorrac(), new ContagiousVorrac());
        harness.setLibrary(player1, topCards);

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(vorrac.getId()));

        assertThat(vorrac.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library still allows proliferating")
    void emptyLibraryStillProliferates() {
        Permanent vorrac = addCounteredVorrac();
        harness.setLibrary(player1, List.of());

        castAndResolve();
        harness.handleMultiplePermanentsChosen(player1, List.of(vorrac.getId()));

        assertThat(vorrac.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library with fewer than four cards permits taking its only land")
    void shortLibraryAllowsTakingLand() {
        Permanent vorrac = addCounteredVorrac();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(vorrac.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Taking one land leaves the other looked-at cards below the untouched library")
    void takingLandBottomsRestBelowUntouchedCards() {
        Forest chosen = new Forest();
        Forest otherLand = new Forest();
        ContagiousVorrac firstNonland = new ContagiousVorrac();
        ContagiousVorrac secondNonland = new ContagiousVorrac();
        Forest untouched = new Forest();
        harness.setLibrary(player1, List.of(chosen, otherLand, firstNonland, secondNonland, untouched));

        castAndResolve();
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(chosen.getId(), otherLand.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 4))
                .containsExactlyInAnyOrder(otherLand, firstNonland, secondNonland);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining a land bottoms all looked-at cards before proliferating")
    void decliningLandBottomsCardsBeforeProliferating() {
        Permanent vorrac = addCounteredVorrac();
        Forest land = new Forest();
        List<Card> lookedAt = List.of(land, new ContagiousVorrac(), new ContagiousVorrac(), new ContagiousVorrac());
        Forest untouched = new Forest();
        harness.setLibrary(player1, List.of(lookedAt.get(0), lookedAt.get(1), lookedAt.get(2), lookedAt.get(3), untouched));

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrderElementsOf(lookedAt);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleMultiplePermanentsChosen(player1, List.of(vorrac.getId()));
        assertThat(vorrac.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Proliferate adds each counter kind to chosen opposing permanents and players")
    void proliferatesChosenOpponentPermanentAndPlayer() {
        Permanent unchosen = addCounteredVorrac();
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new ContagiousVorrac());
        opposing.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        opposing.setCounterCount(CounterType.OIL, 3);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.setLibrary(player1, List.of(new ContagiousVorrac()));

        castAndResolve();
        harness.handleMultiplePermanentsChosen(player1, List.of(opposing.getId(), player2.getId()));

        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(opposing.getCounterCount(CounterType.OIL)).isEqualTo(4);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Proliferate allows choosing no permanents or players")
    void mayChooseNothingToProliferate() {
        Permanent vorrac = addCounteredVorrac();
        harness.setLibrary(player1, List.of(new ContagiousVorrac()));

        castAndResolve();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(vorrac.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Finding no land completes normally when nothing has counters")
    void noLandAndNoCountersCompletesNormally() {
        ContagiousVorrac nonland = new ContagiousVorrac();
        harness.setLibrary(player1, List.of(nonland));

        castAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addCounteredVorrac() {
        Permanent vorrac = harness.addToBattlefieldAndReturn(player1, new ContagiousVorrac());
        vorrac.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        return vorrac;
    }


    private void castAndResolve() {
        harness.castFromHand(player1, new ContagiousVorrac(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
