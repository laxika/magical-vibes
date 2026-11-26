package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.List;
import java.util.Objects;

/**
 * "Conjure a random card from [this]'s spellbook onto the battlefield", "... with mana value X": one random implemented
 * printing of {@code spellbook}, of mana value {@code manaValue} as it resolves when one is given, conjured onto the
 * battlefield by a {@link ConjureCardsEffect} step inserted after this one, so a later step reads it as
 * {@link Subjects#createdThisResolution()}.
 *
 * @param spellbook the printings picked from
 * @param manaValue the mana value the printing must have, or {@code null} for any
 */
public record ConjureRandomCardFromSpellbookWithManaValueToBattlefieldEffect(
        List<DraftFromSpellbookEffect.SpellbookCard> spellbook, DynamicAmount manaValue) implements CardEffect {

    public ConjureRandomCardFromSpellbookWithManaValueToBattlefieldEffect {
        spellbook = List.copyOf(Objects.requireNonNull(spellbook, "Spellbook cannot be null"));
        if (spellbook.isEmpty()) {
            throw new IllegalArgumentException("Spellbook must not be empty");
        }
    }

    /** "Conjure a random card from [this]'s spellbook onto the battlefield." */
    public ConjureRandomCardFromSpellbookWithManaValueToBattlefieldEffect(
            List<DraftFromSpellbookEffect.SpellbookCard> spellbook) {
        this(spellbook, null);
    }
}
