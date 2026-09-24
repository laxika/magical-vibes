package com.github.laxika.magicalvibes.model.effect;

/** Conjures a named card as a real permanent onto the controller's battlefield. */
public record ConjureCardToBattlefieldEffect(String cardName) implements CardEffect {
}
