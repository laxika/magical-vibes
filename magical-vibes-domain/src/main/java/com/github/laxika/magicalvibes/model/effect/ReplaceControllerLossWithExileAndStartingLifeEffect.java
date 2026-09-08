package com.github.laxika.magicalvibes.model.effect;

/**
 * Replacement effect for a permanent that exiles itself when its controller would lose the
 * game, then sets that player's life total to their starting life total.
 */
public record ReplaceControllerLossWithExileAndStartingLifeEffect() implements CardEffect {
}
