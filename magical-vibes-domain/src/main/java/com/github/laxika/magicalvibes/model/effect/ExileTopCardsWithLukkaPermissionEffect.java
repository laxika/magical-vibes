package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top cards of the ability controller's library and grants the exiled creature cards
 * a conditional permission to be cast by that player while they control a Lukka planeswalker.
 */
public record ExileTopCardsWithLukkaPermissionEffect(int count) implements CardEffect {
}
