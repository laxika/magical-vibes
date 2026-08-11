package com.github.laxika.magicalvibes.model.effect;

/**
 * Puts the source permanent and each creature it is blocking on top of their owners' libraries,
 * then shuffles each affected owner's library.
 */
public record PutSourceAndBlockingCreaturesOnTopOfLibraryEffect() implements CardEffect {
}
