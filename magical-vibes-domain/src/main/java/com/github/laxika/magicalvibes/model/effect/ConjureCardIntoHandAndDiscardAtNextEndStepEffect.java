package com.github.laxika.magicalvibes.model.effect;

/** Conjures a card from a known printing into the controller's hand, then discards that card next end step. */
public record ConjureCardIntoHandAndDiscardAtNextEndStepEffect(
        String setCode,
        String collectorNumber
) implements CardEffect {
}
