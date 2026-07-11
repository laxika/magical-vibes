package com.github.laxika.magicalvibes.model.effect;

/**
 * Turns the source permanent's imprinted (face-down exiled) card face up and lets the controller
 * play it without paying its mana cost. Lands are put onto the battlefield (counting as the land
 * play for the turn); other cards are cast from exile. Used by the Hideaway lands' activated
 * "you may play the exiled card" ability (e.g. Howltooth Hollow) — typically wrapped in a
 * {@link MayEffect} for the "you may" wording and a {@link ConditionalEffect} for the card's
 * play condition.
 */
public record PlayImprintedCardWithoutPayingManaCostEffect() implements CardEffect {
}
