package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Set;

/**
 * Registers a one-shot delayed trigger that copies the next spell its controller casts this turn.
 * Permanent spell copies become tokens, and the copy may have new targets chosen.
 */
public record CopyNextSpellCastThisTurnEffect(
        CardPredicate spellFilter,
        Set<CardSupertype> removedSupertypes
) implements CardEffect {

    public CopyNextSpellCastThisTurnEffect() {
        this(null, Set.of());
    }

    public CopyNextSpellCastThisTurnEffect {
        removedSupertypes = removedSupertypes == null ? Set.of() : Set.copyOf(removedSupertypes);
    }
}
