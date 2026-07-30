package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VisionsOfBeyondTest extends BaseCardTest {

    private List<Card> filler(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }

    private void castVisions() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new VisionsOfBeyond()));

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Draws one card when no graveyard has twenty cards")
    void drawsOneBelowThreshold() {
        harness.setGraveyard(player1, filler(19));
        harness.setGraveyard(player2, filler(19));

        castVisions();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws three cards when the controller's graveyard has twenty cards")
    void drawsThreeFromOwnGraveyard() {
        harness.setGraveyard(player1, filler(20));

        castVisions();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Draws three cards when an opponent's graveyard has twenty or more cards")
    void drawsThreeFromOpponentGraveyard() {
        harness.setGraveyard(player2, filler(25));

        castVisions();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Draws only one card when both graveyards are empty")
    void drawsOneWithEmptyGraveyards() {
        castVisions();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
