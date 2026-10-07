package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThassasBounty.class})
class ThassasBountyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards and mills three cards from the target player")
    void drawsAndMillsTargetPlayer() {
        harness.setHand(player1, List.of(new ThassasBounty()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        List<Card> opponentDeck = gd.playerDecks.get(player2.getId());
        harness.setLibrary(player2, opponentDeck.subList(opponentDeck.size() - 10, opponentDeck.size()));

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
        harness.setLibrary(player1, deck.subList(deck.size() - 10, deck.size()));

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }
    @Test
    @DisplayName("Draws the top three cards before milling itself")
    void drawsBeforeMillingItself() {
        Card spell = new ThassasBounty();
        List<Card> drawn = List.of(new ThassasBounty(), new ThassasBounty(), new ThassasBounty());
        List<Card> milled = List.of(new ThassasBounty(), new ThassasBounty(), new ThassasBounty());
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(drawn.get(0), drawn.get(1), drawn.get(2),
                milled.get(0), milled.get(1), milled.get(2)));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(
                milled.get(0), milled.get(1), milled.get(2), spell);
    }

    @Test
    @DisplayName("Mills only the available cards from a short library and still draws three")
    void millsShortLibrary() {
        Card remaining = new ThassasBounty();
        harness.setHand(player1, List.of(new ThassasBounty()));
        harness.setLibrary(player2, List.of(remaining));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remaining);
    }
}
