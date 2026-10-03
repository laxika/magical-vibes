package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BottledCloister.class, BorosRecruit.class})
class BottledCloisterTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the controller's hand face down during an opponent's upkeep")
    void exilesControllerHandDuringOpponentsUpkeep() {
        Permanent cloister = harness.addToBattlefieldAndReturn(player1, new BottledCloister());
        Card controllerCard = new BorosRecruit();
        Card opponentCard = new BorosRecruit();
        harness.setHand(player1, List.of(controllerCard));
        harness.setHand(player2, List.of(opponentCard));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.exiledCards.stream()
                .filter(exiled -> cloister.getId().equals(exiled.sourcePermanentId()))
                .toList()).singleElement().satisfies(exiled -> {
            assertThat(exiled.card()).isSameAs(controllerCard);
            assertThat(exiled.ownerId()).isEqualTo(player1.getId());
            assertThat(exiled.faceDown()).isTrue();
        });
    }

    @Test
    @DisplayName("Exiles every card in the controller's hand")
    void exilesEveryCardInControllerHand() {
        Permanent cloister = harness.addToBattlefieldAndReturn(player1, new BottledCloister());
        Card firstCard = new BorosRecruit();
        Card secondCard = new BorosRecruit();
        harness.setHand(player1, List.of(firstCard, secondCard));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(cloister.getId()))
                .containsExactlyInAnyOrder(firstCard, secondCard);
    }

    @Test
    @DisplayName("Returns owned exiled cards and draws during the controller's upkeep")
    void returnsOwnedExiledCardsAndDrawsDuringOwnUpkeep() {
        Permanent cloister = harness.addToBattlefieldAndReturn(player1, new BottledCloister());
        Card exiledCard = new BorosRecruit();
        Card drawnCard = new BorosRecruit();
        harness.setHand(player1, List.of(exiledCard));
        harness.setLibrary(player1, List.of(drawnCard));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.getCardsExiledByPermanent(cloister.getId())).hasSize(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(exiledCard, drawnCard);
        assertThat(gd.getCardsExiledByPermanent(cloister.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws during the controller's upkeep when no cards are exiled with it")
    void drawsWithNoCardsExiled() {
        harness.addToBattlefield(player1, new BottledCloister());
        Card drawnCard = new BorosRecruit();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Returns cards accumulated across consecutive opponent upkeeps")
    void returnsCardsFromConsecutiveOpponentUpkeeps() {
        Permanent cloister = harness.addToBattlefieldAndReturn(player1, new BottledCloister());
        Card firstCard = new BorosRecruit();
        Card secondCard = new BorosRecruit();
        Card drawnCard = new BorosRecruit();
        harness.setHand(player1, List.of(firstCard));
        harness.setLibrary(player1, List.of(drawnCard));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(secondCard));
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(cloister.getId()))
                .containsExactlyInAnyOrder(firstCard, secondCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(firstCard, secondCard, drawnCard);
        assertThat(gd.getCardsExiledByPermanent(cloister.getId())).isEmpty();
    }

    @Test
    @DisplayName("Uses the second player's hand and upkeep when they control Cloister")
    void worksForSecondPlayerController() {
        Permanent cloister = harness.addToBattlefieldAndReturn(player2, new BottledCloister());
        Card exiledCard = new BorosRecruit();
        Card opponentCard = new BorosRecruit();
        Card drawnCard = new BorosRecruit();
        harness.setHand(player2, List.of(exiledCard));
        harness.setHand(player1, List.of(opponentCard));
        harness.setLibrary(player2, List.of(drawnCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(opponentCard);
        assertThat(gd.getCardsExiledByPermanent(cloister.getId())).containsExactly(exiledCard);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(exiledCard, drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(opponentCard);
        assertThat(gd.getCardsExiledByPermanent(cloister.getId())).isEmpty();
    }
}
