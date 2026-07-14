package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Guards the Card immutability invariant: live cards (frozen at game setup, or when wrapped in a
 * Permanent or referenced by a StackEntry) are shared with AI simulation copies and must never be
 * mutated. See {@code Card.freeze()} and the simulation-leak history in AquitectsWillTest and
 * MimicVatTest.
 */
class CardFreezeTest {

    @Test
    @DisplayName("Mutating a frozen card throws")
    void frozenCardRejectsMutation() {
        Card card = new Card();
        card.setName("Test Card");
        card.freeze();

        assertThatThrownBy(() -> card.setName("Other")).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("frozen");
        assertThatThrownBy(() -> card.setPower(3)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> card.addEffect(EffectSlot.SPELL, new DrawCardEffect(1)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> card.addActivatedAbility(null)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(card::clearRuntimeSpellTargets).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> card.setCastTimeTargetFilter(null)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wrapping a card in a Permanent freezes it")
    void permanentConstructionFreezesCard() {
        Card card = new Card();
        new Permanent(card);

        assertThatThrownBy(() -> card.setPower(1)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Referencing a card from a StackEntry freezes it")
    void stackEntryConstructionFreezesCard() {
        Card card = new Card();
        card.setName("Spell");
        new StackEntry(card, UUID.randomUUID());

        assertThatThrownBy(() -> card.setPower(1)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("createRuntimeCopy of a frozen card is mutable, keeps the id, and leaves the original untouched")
    void runtimeCopyIsMutableAndIndependent() {
        Card original = new Card();
        original.setName("Modal Spell");
        original.addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
        original.freeze();

        Card copy = original.createRuntimeCopy();

        assertThat(copy.getId()).isEqualTo(original.getId());
        assertThat(copy.getName()).isEqualTo("Modal Spell");
        assertThat(copy.getEffects(EffectSlot.SPELL)).hasSize(1);

        // Copy is mutable and its mutations don't reach the original
        copy.setName("Rewritten");
        copy.addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
        assertThat(original.getName()).isEqualTo("Modal Spell");
        assertThat(original.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThatThrownBy(() -> original.setName("X")).isInstanceOf(IllegalStateException.class);
    }

    /**
     * Tripwire: the copy constructor {@code Card(Card)} copies every field by hand. When a field
     * is added to Card, this count changes and the test fails — update the copy constructor AND
     * this constant together.
     */
    @Test
    @DisplayName("Card field count matches the copy constructor (update both when adding fields)")
    void copyConstructorCoversAllFields() {
        long instanceFields = 0;
        for (Field field : Card.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                instanceFields++;
            }
        }
        assertThat(instanceFields)
                .as("Card's instance field count changed — copy the new field in Card(Card source) "
                        + "and update this expected count")
                .isEqualTo(42);
    }
}
