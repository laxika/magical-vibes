package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BottledCloister.class, GrizzlyBears.class})
class BottledCloisterTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the controller's hand face down during an opponent's upkeep")
    void exilesControllerHandDuringOpponentsUpkeep() {
        Permanent cloister = harness.addToBattlefieldAndReturn(player1, new BottledCloister());
        Card controllerCard = new GrizzlyBears();
        Card opponentCard = new GrizzlyBears();
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
    @DisplayName("Returns owned exiled cards and draws during the controller's upkeep")
    void returnsOwnedExiledCardsAndDrawsDuringOwnUpkeep() {
        Permanent cloister = harness.addToBattlefieldAndReturn(player1, new BottledCloister());
        Card exiledCard = new GrizzlyBears();
        Card drawnCard = new GrizzlyBears();
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
}
