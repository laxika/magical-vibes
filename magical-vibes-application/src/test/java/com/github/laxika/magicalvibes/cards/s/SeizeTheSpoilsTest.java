package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeizeTheSpoilsTest extends BaseCardTest {

    @Test
    @DisplayName("Has a discard cost plus draw-two and Treasure effects")
    void hasCorrectStructure() {
        SeizeTheSpoils card = new SeizeTheSpoils();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(3);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DiscardCardTypeCost.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(DrawCardEffect.class);
        assertThat(((DrawCardEffect) card.getEffects(EffectSlot.SPELL).get(1)).amount()).isEqualTo(new Fixed(2));
        assertThat(card.getEffects(EffectSlot.SPELL).get(2)).isInstanceOf(CreateTokenEffect.class);
    }

    @Test
    @DisplayName("Discards a card as a cost, then draws two and makes a Treasure")
    void discardsThenDrawsAndMakesTreasure() {
        harness.setHand(player1, List.of(new SeizeTheSpoils(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Discard the Forest (index 1 in the pre-cast hand) as the additional cost.
        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
        // Started with 2 cards, cast one, discarded one (net 0), then drew two.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Treasure"));
    }

    @Test
    @DisplayName("Cannot be cast with no other card to discard")
    void cannotCastWithoutCardToDiscard() {
        harness.setHand(player1, List.of(new SeizeTheSpoils()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithDiscard(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
