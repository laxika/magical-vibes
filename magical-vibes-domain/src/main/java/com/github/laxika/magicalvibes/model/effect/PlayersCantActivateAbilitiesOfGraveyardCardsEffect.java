package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: no player can activate abilities of cards in graveyards.
 * Prevents activated abilities from being activated on cards in any player's graveyard.
 * Used by Ashes of the Abhorrent (XLN) and similar effects.
 */
public record PlayersCantActivateAbilitiesOfGraveyardCardsEffect() implements CardEffect {
}
