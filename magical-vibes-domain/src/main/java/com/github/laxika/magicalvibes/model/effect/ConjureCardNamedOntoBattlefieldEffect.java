package com.github.laxika.magicalvibes.model.effect;

/** Conjures a card from a known printing directly onto the controller's battlefield. */
public record ConjureCardNamedOntoBattlefieldEffect(
        String setCode,
        String collectorNumber
) implements CardEffect {
}
