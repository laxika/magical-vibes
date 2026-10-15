package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NimbleInnovator.class, Forest.class})
class NimbleInnovatorTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card")
    void etbDrawsACard() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.castFromHand(player1, new NimbleInnovator(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The card is drawn only when the enters trigger resolves")
    void drawWaitsForTriggerResolution() {
        Forest topCard = new Forest();
        Forest nextCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.castFromHand(player1, new NimbleInnovator(), "{3}{U}");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
    }

    @Test
    @DisplayName("Entering without being cast draws for the controller rather than the owner")
    void enteringWithoutCastingDrawsForController() {
        NimbleInnovator innovator = new NimbleInnovator();
        innovator.setOwnerId(player1.getId());
        Forest controllerTopCard = new Forest();
        Forest controllerNextCard = new Forest();
        Forest ownerTopCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(ownerTopCard));
        harness.setLibrary(player2, List.of(controllerTopCard, controllerNextCard));

        harness.enterBattlefieldAndReturn(player2, innovator);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(controllerTopCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(controllerNextCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownerTopCard);
    }
}
