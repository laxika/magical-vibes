package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WeightOfMemoryTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Weight of Memory has correct effects")
    void hasCorrectEffects() {
        WeightOfMemory card = new WeightOfMemory();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DrawCardEffect.class);
        assertThat(((DrawCardEffect) card.getEffects(EffectSlot.SPELL).get(0)).amount()).isEqualTo(3);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(MillTargetPlayerEffect.class);
        assertThat(((MillTargetPlayerEffect) card.getEffects(EffectSlot.SPELL).get(1)).count()).isEqualTo(3);
    }

    // ===== Draw effect =====

    @Test
    @DisplayName("Draws three cards for the caster")
    void drawsThreeCards() {
        harness.setHand(player1, List.of(new WeightOfMemory()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Started with 1 card (Weight of Memory), cast it (0 cards), drew 3
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    // ===== Mill effect =====

    @Test
    @DisplayName("Mills three cards from target player's library")
    void millsThreeCards() {
        harness.setHand(player1, List.of(new WeightOfMemory()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Can target yourself for mill")
    void canTargetSelfForMill() {
        harness.setHand(player1, List.of(new WeightOfMemory()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        // 10 - 3 drawn - 3 milled = 4 remaining in library
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        // 3 milled cards + Weight of Memory itself in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Both draw and mill happen when targeting opponent")
    void bothEffectsHappen() {
        harness.setHand(player1, List.of(new WeightOfMemory()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        List<Card> opponentDeck = gd.playerDecks.get(player2.getId());
        while (opponentDeck.size() > 10) {
            opponentDeck.removeFirst();
        }

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Caster drew 3 cards
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        // Target was milled 3
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Weight of Memory goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new WeightOfMemory()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Weight of Memory"));
        assertThat(gd.stack).isEmpty();
    }
}
