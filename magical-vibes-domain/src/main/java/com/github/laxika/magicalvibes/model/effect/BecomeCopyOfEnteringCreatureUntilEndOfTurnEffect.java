package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;

import java.util.Set;

/**
 * Trigger marker for an optional ability that makes the source a copy of a creature that just
 * entered the battlefield until end of turn. The enter-trigger collector materializes the
 * entering permanent as the target of a {@link BecomeCopyOfTargetCreatureUntilEndOfTurnEffect}.
 */
public record BecomeCopyOfEnteringCreatureUntilEndOfTurnEffect(
        String nameOverride,
        Set<CardSupertype> additionalSupertypesOverride
) implements CardEffect {

    public BecomeCopyOfEnteringCreatureUntilEndOfTurnEffect {
        additionalSupertypesOverride = additionalSupertypesOverride == null
                ? Set.of() : Set.copyOf(additionalSupertypesOverride);
    }

    public BecomeCopyOfEnteringCreatureUntilEndOfTurnEffect() {
        this(null, Set.of());
    }
}
