package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PilferedPlansTest extends BaseCardTest {

    private void castAt(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new PilferedPlans()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Target player mills two cards and the controller draws two")
    void millsTwoAndDrawsTwo() {
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        castAt(player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 2);
    }

    @Test
    @DisplayName("Can target yourself, milling then drawing from your own library")
    void canTargetSelf() {
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        castAt(player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 4);
        harness.assertInGraveyard(player1, "Pilfered Plans");
    }

    @Test
    @DisplayName("Draws two even when the target's library is empty")
    void drawsEvenWithEmptyTargetLibrary() {
        gd.playerDecks.get(player2.getId()).clear();

        castAt(player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Milled cards come from the top of the target's library")
    void millsFromTopOfLibrary() {
        List<Card> deck = gd.playerDecks.get(player2.getId());
        Card top = deck.get(0);
        Card third = deck.get(2);

        castAt(player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(top);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isEqualTo(third);
    }
}
