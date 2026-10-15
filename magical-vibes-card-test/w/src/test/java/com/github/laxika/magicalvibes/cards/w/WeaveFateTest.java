package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeaveFate.class, GrizzlyBears.class, LightningBolt.class})
class WeaveFateTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards when it resolves")
    void drawsTwoCards() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new LightningBolt()));
        harness.castFromHand(player1, new WeaveFate(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Lightning Bolt");
        harness.assertInGraveyard(player1, "Weave Fate");
    }

    @Test
    @DisplayName("Draws the top two cards in order only for its controller")
    void drawsTopTwoCardsOnlyForController() {
        WeaveFate first = new WeaveFate();
        WeaveFate second = new WeaveFate();
        WeaveFate third = new WeaveFate();
        harness.setLibrary(player1, List.of(first, second, third));
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        int opponentLibrarySize = gd.playerDecks.get(player2.getId()).size();

        harness.castFromHand(player1, new WeaveFate(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibrarySize);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Drawing the last two cards does not cause a loss")
    void drawingLastTwoCardsDoesNotCauseLoss() {
        WeaveFate first = new WeaveFate();
        WeaveFate second = new WeaveFate();
        harness.setLibrary(player1, List.of(first, second));

        harness.castFromHand(player1, new WeaveFate(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("With one card remaining, draws it and loses on the second draw")
    void oneCardLibraryCausesLoss() {
        WeaveFate remaining = new WeaveFate();
        harness.setLibrary(player1, List.of(remaining));

        harness.castFromHand(player1, new WeaveFate(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Drawing from an empty library causes its controller to lose")
    void emptyLibraryCausesLoss() {
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new WeaveFate(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
