package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.SpellbookCard;

import java.util.Objects;

/**
 * Puts a fresh card of {@code printing} into its controller's hand because it was drafted from a spellbook; it was
 * not conjured, so "whenever you conjure" abilities don't trigger.
 */
public record DraftCardIntoHandEffect(SpellbookCard printing) implements CardEffect {

    public DraftCardIntoHandEffect {
        Objects.requireNonNull(printing, "printing");
    }
}
