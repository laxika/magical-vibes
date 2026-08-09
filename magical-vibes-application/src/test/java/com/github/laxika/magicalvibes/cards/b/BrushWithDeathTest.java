package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrushWithDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Brush with Death makes a target opponent lose 2 life and its controller gain 2 life")
    void drainsTargetOpponent() {
        harness.setLife(player1, 18);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BrushWithDeath()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Brush with Death cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new BrushWithDeath()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Paying buyback returns Brush with Death to its owner's hand")
    void buybackReturnsToHand() {
        harness.setHand(player1, List.of(new BrushWithDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorceryWithBuyback(player1, 0, player2.getId());
        assertThat(gd.stack.getFirst()).extracting(StackEntry::isBuyback).isEqualTo(true);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Brush with Death");
        harness.assertNotInGraveyard(player1, "Brush with Death");
    }
}
