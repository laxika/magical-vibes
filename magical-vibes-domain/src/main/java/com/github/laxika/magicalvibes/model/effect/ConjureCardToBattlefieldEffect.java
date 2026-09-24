package com.github.laxika.magicalvibes.model.effect;

/** Conjures one full, non-token copy of a registered card printing onto its controller's battlefield. */
public record ConjureCardToBattlefieldEffect(
        String setCode,
        String collectorNumber
) implements CardEffect {
}
