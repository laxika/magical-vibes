package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PureIntentions.class, Distress.class, Forest.class, GrizzlyBears.class, Sift.class, MindRot.class})
class PureIntentionsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns cards discarded because of an opponent's spell")
    void returnsCardsDiscardedByOpponent() {
        harness.setHand(player1, List.of(new PureIntentions(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.setHand(player2, List.of(new Distress()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not return cards discarded by its own controller")
    void doesNotReturnCardsDiscardedByController() {
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new PureIntentions()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.setHand(player1, List.of(new Sift(), new GrizzlyBears(), new Forest(), new Sift()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        harness.setHand(player2, List.of(new Distress()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player2, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Returns itself at the beginning of the next end step when discarded by an opponent")
    void returnsItselfAtNextEndStepWhenDiscardedByOpponent() {
        harness.setHand(player2, List.of(new PureIntentions()));
        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Pure Intentions");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        harness.assertInGraveyard(player2, "Pure Intentions");
        resolveAllTriggers();

        harness.assertNotInGraveyard(player2, "Pure Intentions");
        harness.assertInHand(player2, "Pure Intentions");
    }

    @Test
    @DisplayName("Does not return itself when discarded by its own controller")
    void doesNotReturnItselfWhenDiscardedByController() {
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Sift(), new PureIntentions()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Pure Intentions");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Pure Intentions");
    }

    @Test
    @DisplayName("Returns every card discarded by an opponent this turn")
    void returnsEveryCardDiscardedByOpponentThisTurn() {
        harness.setHand(player1, List.of(new PureIntentions(), new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.setHand(player2, List.of(new MindRot()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Forest");
        harness.assertInGraveyard(player1, "Pure Intentions");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Does not return cards discarded before Pure Intentions resolved")
    void doesNotReturnCardsFromAnEarlierDiscardEvent() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new PureIntentions(), new Sift()));
        harness.setHand(player2, List.of(new Distress(), new Distress()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        harness.assertInHand(player1, "Sift");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A discarded copy returned immediately does not return the cast copy at the end step")
    void delayedReturnDoesNotReturnAnotherCopy() {
        harness.setHand(player1, List.of(new PureIntentions()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);
        harness.setHand(player1, List.of(new PureIntentions()));
        harness.setHand(player2, List.of(new Distress()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        harness.assertInHand(player1, "Pure Intentions");
        harness.assertInGraveyard(player1, "Pure Intentions");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Pure Intentions");
    }

    @Test
    @DisplayName("The discard protection expires after the turn in which it resolved")
    void protectionExpiresAfterTheTurn() {
        harness.setHand(player1, List.of(new PureIntentions(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Distress()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }
}
