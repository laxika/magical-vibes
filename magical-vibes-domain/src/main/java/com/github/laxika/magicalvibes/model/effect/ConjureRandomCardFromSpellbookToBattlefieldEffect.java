package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.Objects;

/**
 * Conjures one random implemented card from the listed printings onto the resolving controller's
 * battlefield.
 */
public record ConjureRandomCardFromSpellbookToBattlefieldEffect(
        List<DraftFromSpellbookEffect.SpellbookCard> spellbook, int basePower, int baseToughness)
        implements CardEffect {

    public ConjureRandomCardFromSpellbookToBattlefieldEffect {
        spellbook = List.copyOf(Objects.requireNonNull(spellbook, "Spellbook cannot be null"));
        if (spellbook.isEmpty()) {
            throw new IllegalArgumentException("Spellbook must not be empty");
        }
    }
}
