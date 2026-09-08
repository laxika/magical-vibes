package com.github.laxika.magicalvibes.model.effect;

/**
 * Graveyard-entry trigger effect that exiles the card that caused the trigger if it is still in a
 * graveyard when the ability resolves.
 */
public record ExileTriggeringCardFromGraveyardEffect() implements CardEffect {
}
