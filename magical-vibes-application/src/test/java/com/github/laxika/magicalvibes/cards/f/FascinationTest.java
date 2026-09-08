package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FascinationTest extends BaseCardTest {

    @Test
    @DisplayName("Draw mode makes each player draw X cards")
    void drawModeMakesEachPlayerDrawXCards() {
        harness.setHand(player1, List.of(new Fascination()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        int player1HandBefore = gd.playerHands.get(player1.getId()).size() - 1;
        int player2HandBefore = gd.playerHands.get(player2.getId()).size();

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{0}, 2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBefore + 2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore + 2);
    }

    @Test
    @DisplayName("Mill mode makes each player mill X cards")
    void millModeMakesEachPlayerMillXCards() {
        harness.setHand(player1, List.of(new Fascination()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        int player1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int player2DeckBefore = gd.playerDecks.get(player2.getId()).size();
        int player1GraveyardBefore = gd.playerGraveyards.get(player1.getId()).size();
        int player2GraveyardBefore = gd.playerGraveyards.get(player2.getId()).size();

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{1}, 2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckBefore - 2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckBefore - 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(player1GraveyardBefore + 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(player2GraveyardBefore + 2);
    }

    @Test
    @DisplayName("X=0 makes the chosen mode do nothing")
    void xZeroDoesNothing() {
        harness.setHand(player1, List.of(new Fascination()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        int player1HandBefore = gd.playerHands.get(player1.getId()).size() - 1;
        int player2HandBefore = gd.playerHands.get(player2.getId()).size();

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{0}, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore);
    }
}
