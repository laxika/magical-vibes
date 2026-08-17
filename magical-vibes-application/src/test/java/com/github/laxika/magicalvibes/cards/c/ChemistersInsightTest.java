package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChemistersInsightTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards when cast from hand")
    void drawsTwoCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new ChemistersInsight()));
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Chemister's Insight");
    }

    @Test
    @DisplayName("Jump-start discards any card, draws two, and exiles the spell")
    void jumpStartDiscardsDrawsAndExiles() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setGraveyard(player1, List.of(new ChemistersInsight()));
        harness.setHand(player1, List.of(new Plains()));
        addMana();

        harness.castJumpStart(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Plains");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Chemister's Insight"));
    }

    @Test
    @DisplayName("Jump-start requires a card in hand to discard")
    void jumpStartRequiresDiscard() {
        harness.setGraveyard(player1, List.of(new ChemistersInsight()));
        harness.setHand(player1, List.of());
        addMana();

        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
