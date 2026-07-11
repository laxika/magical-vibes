package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmbraceTheParadoxTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three then offers a may land drop")
    void hasCorrectStructure() {
        EmbraceTheParadox card = new EmbraceTheParadox();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DrawCardEffect.class);
        assertThat(((DrawCardEffect) card.getEffects(EffectSlot.SPELL).get(0)).amount()).isEqualTo(new Fixed(3));
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(MayEffect.class);
    }

    @Test
    @DisplayName("Draws three cards and puts a chosen land onto the battlefield tapped")
    void drawsAndPutsLandTapped() {
        harness.setHand(player1, List.of(new EmbraceTheParadox(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        // Hand is now [Forest, drawn1, drawn2, drawn3]; put the Forest (index 0) onto the battlefield.
        harness.handleCardChosen(player1, 0);

        Permanent forest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest")).findFirst().orElseThrow();
        assertThat(forest.isTapped()).isTrue();
        // Started with 1 (Forest) + drew 3 - 1 put onto battlefield = 3 cards in hand.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Declining the land drop leaves the land in hand")
    void decliningLeavesLandInHand() {
        harness.setHand(player1, List.of(new EmbraceTheParadox(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }
}
