package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SandstoneOracle.class, GrizzlyBears.class, Forest.class})
class SandstoneOracleTest extends BaseCardTest {

    @Test
    @DisplayName("ETB: draws the difference when the opponent has more cards")
    void drawsDifferenceWhenOpponentHasMoreCards() {
        SandstoneOracle oracle = new SandstoneOracle();
        harness.setHand(player1, cards(oracle, 2));
        harness.setHand(player2, cards(5));
        harness.setLibrary(player1, cards(10));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);
    }

    @Test
    @DisplayName("ETB: draws nothing when the opponent has no more cards")
    void drawsNothingWhenOpponentHasNoMoreCards() {
        SandstoneOracle oracle = new SandstoneOracle();
        harness.setHand(player1, cards(oracle, 4));
        harness.setHand(player2, cards(2));
        harness.setLibrary(player1, cards(10));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(10);
    }

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }

    private List<Card> cards(Card first, int additionalCount) {
        List<Card> cards = cards(additionalCount);
        cards.add(0, first);
        return cards;
    }


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
    void drawsDifferenceWhenOpponentHasMoreCardsWithSmallerHands() {
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
