package com.github.laxika.magicalvibes.model.effect;

/** Creates a fresh card with the named card's characteristics directly on the battlefield. */
public record ConjureCardNamedOntoBattlefieldEffect(String cardName) implements CardEffect {
}
