package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeddingRing.class})
class WeddingRingTest extends BaseCardTest {

    @Test
    @DisplayName("A cast Wedding Ring gives the targeted opponent a token copy")
    void castCreatesTokenCopyForTargetOpponent() {
        castWeddingRing();

        List<Permanent> opponentRings = findPermanents(player2, "Wedding Ring");
        assertThat(opponentRings).hasSize(1);
        assertThat(opponentRings.getFirst().getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("During the opponent's turn, their Wedding Ring draw gives you a card")
    void drawsWhenOpponentDrawsDuringTheirTurn() {
        castWeddingRing();
        int controllerHandSize = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandSize + 1);
    }

    @Test
    @DisplayName("During the opponent's turn, their Wedding Ring life gain gives you that much life")
    void gainsLifeWhenOpponentGainsLifeDuringTheirTurn() {
        castWeddingRing();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Wedding Ring does not trigger for an opponent's draw or life gain outside their turn")
    void doesNotTriggerOutsideOpponentsTurn() {
        castWeddingRing();
        int controllerHandSize = gd.playerHands.get(player1.getId()).size();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandSize);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    private void castWeddingRing() {
        harness.setHand(player1, List.of(new WeddingRing()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.WHITE, 2);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
