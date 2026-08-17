package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RadicalIdeaTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when cast from hand")
    void drawsACard() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new RadicalIdea()));
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
        harness.assertInGraveyard(player1, "Radical Idea");
    }

    @Test
    @DisplayName("Jump-start discards a card, draws a card, and exiles Radical Idea")
    void jumpStartDiscardsDrawsAndExiles() {
        RadicalIdea spell = new RadicalIdea();
        Plains discarded = new Plains();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        addMana();

        harness.castJumpStart(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(discarded.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Jump-start requires a card in hand to discard")
    void jumpStartRequiresDiscard() {
        harness.setGraveyard(player1, List.of(new RadicalIdea()));
        harness.setHand(player1, List.of());
        addMana();

        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
