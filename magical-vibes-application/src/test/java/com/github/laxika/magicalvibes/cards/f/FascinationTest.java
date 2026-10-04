package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Fascination.class})
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

    @Test
    @DisplayName("Mill mode with X=0 leaves both libraries unchanged")
    void zeroMillLeavesLibrariesUnchanged() {
        Fascination spell = new Fascination();
        Fascination first = new Fascination();
        Fascination second = new Fascination();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(first));
        harness.setLibrary(player2, List.of(second));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{1}, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mill mode mills only available cards when a library has fewer than X")
    void millStopsAtEndOfEachLibrary() {
        Fascination spell = new Fascination();
        Fascination first = new Fascination();
        Fascination second = new Fascination();
        Fascination third = new Fascination();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(first));
        harness.setLibrary(player2, List.of(second, third));
        harness.addMana(player1, ManaColor.BLUE, 5);
        int player1HandBefore = gd.playerHands.get(player1.getId()).size() - 1;
        int player2HandBefore = gd.playerHands.get(player2.getId()).size();

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{1}, 3, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(second, third);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore);
    }
}
