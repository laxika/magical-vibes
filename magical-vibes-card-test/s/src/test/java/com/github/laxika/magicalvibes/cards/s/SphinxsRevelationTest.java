package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SphinxsRevelation.class})
class SphinxsRevelationTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack with the chosen X value")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new SphinxsRevelation()));
        addRevelationMana(3);

        harness.castInstant(player1, 0, 3, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("X=3 gains 3 life and draws 3 cards")
    void xThreeGainsLifeAndDraws() {
        harness.setHand(player1, List.of(new SphinxsRevelation()));
        addRevelationMana(3);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size() - 1;
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        harness.assertInGraveyard(player1, "Sphinx's Revelation");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("X=0 gains no life and draws no cards")
    void xZeroDoesNothing() {
        harness.setHand(player1, List.of(new SphinxsRevelation()));
        addRevelationMana(0);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size() - 1;
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("The other player gains life and draws cards when they cast it")
    void otherControllerGainsLifeAndDraws() {
        SphinxsRevelation drawnCard = new SphinxsRevelation();
        harness.setHand(player2, List.of(new SphinxsRevelation()));
        harness.setLibrary(player2, List.of(drawnCard, new SphinxsRevelation()));
        harness.setLife(player1, 12);
        harness.setLife(player2, 7);
        int playerOneHandSize = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, 1, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 8);
        harness.assertLife(player1, 12);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(playerOneHandSize);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Sphinx's Revelation");
    }

    @Test
    @DisplayName("X=0 with an empty library does not attempt a draw")
    void xZeroWithEmptyLibraryDoesNotLose() {
        harness.setHand(player1, List.of(new SphinxsRevelation()));
        harness.setLibrary(player1, List.of());
        addRevelationMana(0);
        var statusBefore = gd.status;
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.status).isEqualTo(statusBefore);
        assertThat(gd.winnerPlayerId).isNull();
        harness.assertLife(player1, lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Sphinx's Revelation");
    }

    private void addRevelationMana(int xValue) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        if (xValue > 0) {
            harness.addMana(player1, ManaColor.COLORLESS, xValue);
        }
    }
}
