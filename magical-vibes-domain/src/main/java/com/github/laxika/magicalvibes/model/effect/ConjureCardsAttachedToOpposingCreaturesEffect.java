package com.github.laxika.magicalvibes.model.effect;

/** Conjures one named card attached to each opposing creature. */
public record ConjureCardsAttachedToOpposingCreaturesEffect(String cardName) implements CardEffect {
}
