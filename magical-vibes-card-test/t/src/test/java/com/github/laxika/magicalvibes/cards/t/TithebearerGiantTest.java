package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TithebearerGiant.class, Forest.class})
class TithebearerGiantTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws a card and loses 1 life")
    void etbDrawsAndLosesLife() {
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new TithebearerGiant(), "{5}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Drawing and life loss wait for the enter trigger to resolve")
    void enterTriggerUsesTheStack() {
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new TithebearerGiant(), "{5}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The other player draws and loses life when they control the Giant")
    void otherControllerDrawsAndLosesLife() {
        Forest drawnCard = new Forest();
        harness.forceActivePlayer(player2);
        harness.setLibrary(player2, List.of(drawnCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player2, new TithebearerGiant(), "{5}{B}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawnCard);
    }

    @Test
    @DisplayName("An empty library does not stop the trigger's life loss")
    void emptyLibraryStillLosesLife() {
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new TithebearerGiant(), "{5}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("At one life the controller draws before losing the game")
    void drawsBeforeLosingAtOneLife() {
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLife(player1, 1);
        harness.castFromHand(player1, new TithebearerGiant(), "{5}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.getLife(player1.getId())).isZero();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
