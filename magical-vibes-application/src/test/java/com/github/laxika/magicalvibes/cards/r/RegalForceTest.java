package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RegalForceTest extends BaseCardTest {

    private void stockDeckWithForests(int count) {
        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < count; i++) {
            gd.playerDecks.get(player1.getId()).add(new Forest());
        }
    }

    @Test
    @DisplayName("ETB draws a card for each green creature, counting itself")
    void etbDrawsForEachGreenCreatureIncludingItself() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        stockDeckWithForests(5);
        harness.setHand(player1, List.of(new RegalForce()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        // Two Grizzly Bears + Regal Force itself = 3 green creatures.
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore - 1 + 3);
    }

    @Test
    @DisplayName("Non-green creatures are not counted")
    void nonGreenCreaturesNotCounted() {
        addCreatureReady(player1, new HillGiant());
        stockDeckWithForests(5);
        harness.setHand(player1, List.of(new RegalForce()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        // Only Regal Force itself is green.
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore - 1 + 1);
    }
}
