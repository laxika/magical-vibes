package com.github.laxika.magicalvibes.model.effect;

/** Conjures one full, non-token copy of a registered card printing into its controller's hand. */
public record ConjureCardToHandEffect(
        String setCode,
        String collectorNumber
) implements CardEffect {
}
