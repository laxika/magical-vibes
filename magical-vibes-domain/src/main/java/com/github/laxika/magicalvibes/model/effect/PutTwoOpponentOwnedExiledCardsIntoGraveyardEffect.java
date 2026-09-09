package com.github.laxika.magicalvibes.model.effect;

/**
 * Offers to put exactly two face-up cards owned by opponents from exile into their owners'
 * graveyards. The resolving entry records whether both cards were moved for a following
 * conditional effect.
 */
public record PutTwoOpponentOwnedExiledCardsIntoGraveyardEffect() implements CardEffect {
}
