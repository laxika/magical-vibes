package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WheelAndDeal.class, Forest.class, Island.class, Mountain.class})
class WheelAndDealTest extends BaseCardTest {

    @Test
    @DisplayName("Targeted opponent discards their hand, draws seven, and controller draws a card")
    void opponentDiscardsAndDrawsSeven() {
        Card controllerDraw = new Forest();
        List<Card> opponentDraws = List.of(
                new Island(), new Island(), new Island(), new Island(),
                new Island(), new Island(), new Island());
        List<Card> discarded = List.of(new Mountain(), new Mountain());
        harness.setLibrary(player1, List.of(controllerDraw));
        harness.setLibrary(player2, opponentDraws);
        harness.setHand(player1, List.of(new WheelAndDeal()));
        harness.setHand(player2, discarded);
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(player2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerDraw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(opponentDraws);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(discarded);
    }

    @Test
    @DisplayName("No targets still resolves the controller's draw")
    void noTargetsStillDraws() {
        Card controllerDraw = new Forest();
        List<Card> opponentHand = List.of(new Mountain(), new Mountain());
        harness.setLibrary(player1, List.of(controllerDraw));
        harness.setHand(player1, List.of(new WheelAndDeal()));
        harness.setHand(player2, opponentHand);
        addMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerDraw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(opponentHand);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent with an empty hand still draws seven cards")
    void emptyOpponentHandStillDrawsSeven() {
        Card controllerDraw = new Forest();
        List<Card> opponentDraws = List.of(
                new Island(), new Island(), new Island(), new Island(),
                new Island(), new Island(), new Island());
        harness.setLibrary(player1, List.of(controllerDraw));
        harness.setLibrary(player2, opponentDraws);
        harness.setHand(player1, List.of(new WheelAndDeal()));
        harness.setHand(player2, List.of());
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(player2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerDraw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(opponentDraws);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new WheelAndDeal()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The other player can cast it without discarding their remaining hand")
    void otherControllerRetainsHandAndDrawsOne() {
        Card retained = new Mountain();
        Card controllerDraw = new Forest();
        List<Card> opponentDraws = List.of(
                new Island(), new Island(), new Island(), new Island(),
                new Island(), new Island(), new Island());
        List<Card> discarded = List.of(new Mountain(), new Forest());
        harness.setLibrary(player2, List.of(controllerDraw));
        harness.setLibrary(player1, opponentDraws);
        harness.setHand(player2, List.of(new WheelAndDeal(), retained));
        harness.setHand(player1, discarded);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player2, 0, List.of(player1.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained, controllerDraw);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(opponentDraws);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(discarded);
        harness.assertInGraveyard(player2, "Wheel and Deal");
    }

    @Test
    @DisplayName("The same opponent cannot be targeted twice")
    void cannotTargetOpponentTwice() {
        harness.setHand(player1, List.of(new WheelAndDeal()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
