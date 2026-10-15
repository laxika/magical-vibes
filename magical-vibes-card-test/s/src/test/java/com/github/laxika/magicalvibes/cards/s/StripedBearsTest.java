package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Striped Bears")
@CardUsed({StripedBears.class})
class StripedBearsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card")
    void etbDrawsACard() {
        harness.setLibrary(player1, List.of(new StripedBears()));
        harness.castFromHand(player1, new StripedBears(), "{3}{G}");

        harness.passBothPriorities(); // resolve creature spell → ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger → draw

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Striped Bears");
        harness.assertInHand(player1, "Striped Bears");
    }

    @Test
    @DisplayName("Draw waits for the enter trigger to resolve and draws exactly one card")
    void drawWaitsForTriggerResolution() {
        StripedBears topCard = new StripedBears();
        StripedBears secondCard = new StripedBears();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.castFromHand(player1, new StripedBears(), "{3}{G}");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Striped Bears");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
    }

    @Test
    @DisplayName("Entering without being cast draws for the entering creature's controller")
    void enteringWithoutCastingDrawsForController() {
        StripedBears drawnCard = new StripedBears();
        StripedBears opponentCard = new StripedBears();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(opponentCard));
        harness.setLibrary(player2, List.of(drawnCard));

        harness.enterBattlefieldAndReturn(player2, new StripedBears());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Striped Bears");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentCard);
    }
}
