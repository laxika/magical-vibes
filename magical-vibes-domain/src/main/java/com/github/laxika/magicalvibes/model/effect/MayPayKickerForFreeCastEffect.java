package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for the optional kicker payment offered while a spell is cast without paying its mana cost
 * (kicker is an optional additional cost, so it may still be paid; CR 118.9, CR 601.2b). Queued by
 * {@code ExileFreeCastSupport}; the pending ability's target card is the exiled spell being cast.
 */
public record MayPayKickerForFreeCastEffect() implements CardEffect {
}
