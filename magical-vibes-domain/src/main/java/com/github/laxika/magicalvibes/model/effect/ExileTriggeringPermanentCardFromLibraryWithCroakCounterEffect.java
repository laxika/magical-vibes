package com.github.laxika.magicalvibes.model.effect;

/**
 * Library-to-graveyard trigger effect that exiles the triggering permanent card with a croak
 * counter if it is still in that graveyard when the ability resolves.
 */
public record ExileTriggeringPermanentCardFromLibraryWithCroakCounterEffect() implements CardEffect {
}
