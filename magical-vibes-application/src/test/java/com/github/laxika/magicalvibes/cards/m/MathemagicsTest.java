package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DrawTwoToTheXCardsForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MathemagicsTest extends BaseCardTest {

    @Test
    @DisplayName("Targets a player and draws via the 2^X effect")
    void hasCorrectStructure() {
        Mathemagics card = new Mathemagics();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getTargetFilter()).isInstanceOf(PlayerPredicateTargetFilter.class);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(DrawTwoToTheXCardsForTargetPlayerEffect.class);
    }

    @Test
    @DisplayName("With X=2, target player draws 4 cards")
    void drawsTwoToTheXWithXTwo() {
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        harness.setHand(player1, List.of(new Mathemagics()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 4);
    }

    @Test
    @DisplayName("With X=0, target player draws 1 card")
    void drawsOneWithXZero() {
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        harness.setHand(player1, List.of(new Mathemagics()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 1);
    }
}
