package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DiscardCardUnlessAttackedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChartACourseTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has DrawCardEffect(2) and DiscardCardUnlessAttackedThisTurnEffect on SPELL slot")
    void hasCorrectEffects() {
        ChartACourse card = new ChartACourse();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DrawCardEffect.class);
        DrawCardEffect drawEffect = (DrawCardEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(drawEffect.amount()).isEqualTo(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(DiscardCardUnlessAttackedThisTurnEffect.class);
    }

    // ===== Did not attack this turn — must discard =====

    @Test
    @DisplayName("When player did not attack this turn, draws 2 then must discard 1")
    void noAttack_mustDiscard() {
        harness.setHand(player1, List.of(new ChartACourse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Should be awaiting discard choice (did not attack)
        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.DISCARD_CHOICE)).isTrue();

        // Hand should have 2 cards (0 original after casting + 2 drawn)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        // Choose to discard the first card
        harness.handleCardChosen(player1, 0);

        // After discard, hand should be 1
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    // ===== Attacked this turn — no discard =====

    @Test
    @DisplayName("When player attacked this turn, draws 2 and does not have to discard")
    void attacked_noDiscard() {
        harness.setHand(player1, List.of(new ChartACourse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Mark player1 as having attacked this turn
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Should NOT be awaiting discard — attack condition met
        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.DISCARD_CHOICE)).isFalse();

        // Hand should have 2 cards (0 original after casting + 2 drawn)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    // ===== Attacked this turn with cards in hand — all cards kept =====

    @Test
    @DisplayName("When player attacked this turn and has other cards, all are kept")
    void attacked_withOtherCards_allKept() {
        Card otherCard = new Shock();
        harness.setHand(player1, List.of(new ChartACourse(), otherCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Mark player1 as having attacked this turn
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Hand should have 3 cards (1 remaining + 2 drawn)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.DISCARD_CHOICE)).isFalse();
    }

    // ===== Spell goes to graveyard after resolution =====

    @Test
    @DisplayName("Chart a Course goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new ChartACourse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Attacked so no discard interaction needed
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Chart a Course"));
    }
}
