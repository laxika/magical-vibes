package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThassasBountyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards and mills three cards from the target player")
    void drawsAndMillsTargetPlayer() {
        harness.setHand(player1, List.of(new ThassasBounty()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        List<Card> opponentDeck = gd.playerDecks.get(player2.getId());
        while (opponentDeck.size() > 10) {
            opponentDeck.removeFirst();
        }

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Can target its controller for the mill")
    void canTargetController() {
        harness.setHand(player1, List.of(new ThassasBounty()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }
}
