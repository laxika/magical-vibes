package com.github.laxika.magicalvibes.model.effect;

/**
 * Death-trigger effect that puts the card of the creature that died, the stack entry's triggering card, from its
 * controller's graveyard on top of that player's library.
 */
public record PutTriggeringCardFromGraveyardOnTopOfLibraryEffect() implements CardEffect {
}
