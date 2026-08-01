package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    private void addRevelationMana(int xValue) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        if (xValue > 0) {
            harness.addMana(player1, ManaColor.COLORLESS, xValue);
        }
    }
}
