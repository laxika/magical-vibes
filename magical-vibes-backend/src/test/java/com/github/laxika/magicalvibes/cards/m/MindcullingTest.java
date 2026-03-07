package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MindcullingTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Mindculling has correct effects and target filter")
    void hasCorrectProperties() {
        Mindculling card = new Mindculling();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getTargetFilter()).isInstanceOf(PlayerPredicateTargetFilter.class);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DrawCardEffect.class);
        assertThat(((DrawCardEffect) card.getEffects(EffectSlot.SPELL).get(0)).amount()).isEqualTo(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(TargetPlayerDiscardsEffect.class);
        assertThat(((TargetPlayerDiscardsEffect) card.getEffects(EffectSlot.SPELL).get(1)).amount()).isEqualTo(2);
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new Mindculling()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Caster draws two cards and target opponent is prompted to discard two")
    void drawsAndOpponentDiscards() {
        harness.setHand(player1, List.of(new Mindculling()));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest())));
        harness.addMana(player1, ManaColor.BLUE, 6);

        int player1HandBefore = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Caster should have drawn 2 cards (had 0 after casting, now 2)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBefore - 1 + 2);

        // Target player should be prompted to discard
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.awaitingCardChoicePlayerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.discardRemainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Opponent with empty hand still lets caster draw two")
    void opponentEmptyHandStillDraws() {
        harness.setHand(player1, List.of(new Mindculling()));
        harness.setHand(player2, new ArrayList<>(List.of()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Caster still draws 2
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        // No discard prompt
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    @Test
    @DisplayName("Opponent with one card discards it then discard ends")
    void opponentWithOneCardDiscardsIt() {
        harness.setHand(player1, List.of(new Mindculling()));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Caster drew 2
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        // Opponent prompted to discard
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        harness.handleCardChosen(player2, 0);

        // Hand empty, second discard skipped
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mindculling goes to caster's graveyard after resolving")
    void goesToCasterGraveyard() {
        harness.setHand(player1, List.of(new Mindculling()));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mindculling"));
    }
}
