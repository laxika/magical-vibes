package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JacesIngenuityTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Jace's Ingenuity has correct effects")
    void hasCorrectEffects() {
        JacesIngenuity card = new JacesIngenuity();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(DrawCardEffect.class);
        DrawCardEffect effect = (DrawCardEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.amount()).isEqualTo(3);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Jace's Ingenuity puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Jace's Ingenuity");
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving draws three cards")
    void resolvingDrawsThreeCards() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // Hand should have 3 cards (spell left hand, then drew 3)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        // Deck should have lost 3 cards
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
    }

    @Test
    @DisplayName("Jace's Ingenuity goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Jace's Ingenuity"));
    }
}
