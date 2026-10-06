package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Cremate;
import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RelicRetriever.class, Disentomb.class, GrizzlyBears.class, Cremate.class})
class RelicRetrieverTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure at the end step after a card leaves your graveyard")
    void createsTreasureAfterCardLeavesGraveyard() {
        addRetriever();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Treasure when no card left your graveyard this turn")
    void doesNotCreateTreasureWithoutGraveyardDeparture() {
        addRetriever();

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Does not count a card leaving an opponent's graveyard")
    void doesNotCountOpponentsGraveyardDeparture() {
        addRetriever();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player2, List.of(new Disentomb()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, creature.getId());

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Multiple graveyard departures create only one Treasure per Retriever")
    void multipleDeparturesCreateOneTreasure() {
        addRetriever();
        Card first = new RelicRetriever();
        Card second = new RelicRetriever();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Disentomb(), new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, first.getId());
        harness.castAndResolveSorcery(player1, 0, second.getId());
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Counts a departure earlier in the turn before Retriever entered")
    void countsDepartureBeforeEntering() {
        Card creature = new RelicRetriever();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());
        addRetriever();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Exiling a noncreature from your graveyard on an opponent's turn counts only that turn")
    void triggersOnOpponentsEndStepAndResetsNextTurn() {
        addRetriever();
        Card card = new Disentomb();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new Cremate()));
        harness.setLibrary(player1, List.of(new RelicRetriever(), new RelicRetriever()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, card.getId());
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("A first graveyard departure after the end step began does not trigger")
    void departureDuringEndStepDoesNotTriggerRetroactively() {
        addRetriever();
        Card card = new Disentomb();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new Cremate()));
        harness.setLibrary(player1, List.of(new RelicRetriever()));

        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, card.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private void addRetriever() {
        harness.addToBattlefield(player1, new RelicRetriever());
    }
}
