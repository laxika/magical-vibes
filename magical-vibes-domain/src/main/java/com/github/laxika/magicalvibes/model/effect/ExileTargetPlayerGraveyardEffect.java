package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile all cards from target player's graveyard.
 * Used by Nihil Spellbomb and similar graveyard hate cards.
 */
public record ExileTargetPlayerGraveyardEffect() implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
