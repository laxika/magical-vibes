package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SandstoneOracle.class, Forest.class})
class SandstoneOracleTest extends BaseCardTest {

    private List<Forest> forests(int count) {
        List<Forest> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Forest());
        }
        return cards;
    }

    private void castSandstoneOracle() {
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Draws the hand-size difference when the opponent has more cards")
    void drawsDifferenceWhenOpponentHasMoreCards() {
        harness.setHand(player1, new ArrayList<>(List.of(new SandstoneOracle(), new Forest())));
        harness.setHand(player2, new ArrayList<>(forests(4)));
        harness.setLibrary(player1, new ArrayList<>(forests(5)));

        castSandstoneOracle();

        // Opponent 4, controller 1 after casting -> draw 3.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Draws nothing when the opponent does not have more cards")
    void drawsNothingWhenOpponentHasFewerOrEqualCards() {
        harness.setHand(player1, new ArrayList<>(List.of(
                new SandstoneOracle(), new Forest(), new Forest())));
        harness.setHand(player2, new ArrayList<>(forests(2)));
        harness.setLibrary(player1, new ArrayList<>(forests(5)));

        castSandstoneOracle();

        // Opponent 2, controller 2 after casting -> draw 0.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }
}
