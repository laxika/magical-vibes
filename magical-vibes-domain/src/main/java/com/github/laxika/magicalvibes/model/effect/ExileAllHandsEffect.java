package com.github.laxika.magicalvibes.model.effect;

/**
 * "Exile all cards from all hands." Every card in every player's hand is exiled; there is no player
 * choice and no play permission. Exiling from hand is not a discard, so no discard triggers fire.
 * The non-targeting, all-players counterpart of {@link ExileTargetPlayerHandEffect}. Used by
 * Worldfire, where it is paired with an {@link ExileGraveyardCardsEffect}
 * ({@code ALL_PLAYERS}).
 */
public record ExileAllHandsEffect() implements CardEffect {
}
