package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VisionsOfBeyond.class, RuneclawBear.class})
class VisionsOfBeyondTest extends BaseCardTest {

    private List<Card> filler(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new RuneclawBear());
        }
        return cards;
    }

    private void castVisions() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new VisionsOfBeyond(), "{U}");

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

    @Test
    @DisplayName("Draws three cards, not six, when both graveyards meet the threshold")
    void drawsThreeWhenBothGraveyardsQualify() {
        harness.setGraveyard(player1, filler(20));
        harness.setGraveyard(player2, filler(20));

        castVisions();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Does not count the resolving spell toward the graveyard threshold")
    void resolvingSpellDoesNotCount() {
        harness.setGraveyard(player1, filler(19));

        castVisions();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(20);
        harness.assertInGraveyard(player1, "Visions of Beyond");
    }

    @Test
    @DisplayName("Checks the threshold at resolution when a graveyard grows after casting")
    void graveyardReachesThresholdBeforeResolution() {
        harness.setGraveyard(player2, filler(19));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new VisionsOfBeyond(), "{U}");

        harness.setGraveyard(player2, filler(20));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Checks the threshold at resolution when a graveyard shrinks after casting")
    void graveyardFallsBelowThresholdBeforeResolution() {
        harness.setGraveyard(player2, filler(20));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new VisionsOfBeyond(), "{U}");

        harness.setGraveyard(player2, filler(19));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
