package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MysteriesOfTheDeepTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards when no land entered under the controller's control this turn")
    void drawsTwoCardsWithoutLandfall() {
        harness.setHand(player1, List.of(new MysteriesOfTheDeep()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        addManaForSpell();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(2);
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Draws three cards after a land entered under the controller's control this turn")
    void drawsThreeCardsWithLandfall() {
        harness.setHand(player1, List.of(new Forest(), new MysteriesOfTheDeep()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.playLand(player1, 0);
        addManaForSpell();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(3);
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).hasSize(1);
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
