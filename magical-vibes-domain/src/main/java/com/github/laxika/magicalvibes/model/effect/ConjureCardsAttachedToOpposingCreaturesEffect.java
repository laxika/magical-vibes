package com.github.laxika.magicalvibes.model.effect;

/** Conjures one card of the given printing attached to each opposing creature. */
public record ConjureCardsAttachedToOpposingCreaturesEffect(String setCode, String collectorNumber)
        implements CardEffect {
}
