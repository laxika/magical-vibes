package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;

import java.util.Set;

/**
 * Causes the source permanent to become a copy of the target creature until end of turn.
 * At the cleanup step, the permanent reverts to its original card.
 * Used by Tilonalli's Skinshifter and similar shapeshifters.
 */
public record BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(
        String nameOverride,
        Set<CardSupertype> additionalSupertypesOverride
) implements CardEffect {

    public BecomeCopyOfTargetCreatureUntilEndOfTurnEffect {
        additionalSupertypesOverride = additionalSupertypesOverride == null
                ? Set.of() : Set.copyOf(additionalSupertypesOverride);
    }

    public BecomeCopyOfTargetCreatureUntilEndOfTurnEffect() {
        this(null, Set.of());
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
