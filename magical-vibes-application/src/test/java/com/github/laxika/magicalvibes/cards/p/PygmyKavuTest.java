package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PygmyKavuTest extends BaseCardTest {

    private void stockDeckWithForests(int count) {
        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < count; i++) {
            gd.playerDecks.get(player1.getId()).add(new Forest());
        }
    }

    @Test
    @DisplayName("ETB draws a card for each black creature opponents control")
    void etbDrawsForEachBlackCreatureOpponentsControl() {
        addCreatureReady(player2, new Gravecrawler());
        addCreatureReady(player2, new Gravecrawler());
        addCreatureReady(player2, new GrizzlyBears());
        stockDeckWithForests(5);
        harness.setHand(player1, List.of(new PygmyKavu()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore - 1 + 2);
    }

    @Test
    @DisplayName("ETB draws no cards when opponents control no black creatures")
    void etbDrawsNoCardsWithoutBlackOpposingCreatures() {
        addCreatureReady(player2, new GrizzlyBears());
        stockDeckWithForests(5);
        harness.setHand(player1, List.of(new PygmyKavu()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore - 1);
    }
}
