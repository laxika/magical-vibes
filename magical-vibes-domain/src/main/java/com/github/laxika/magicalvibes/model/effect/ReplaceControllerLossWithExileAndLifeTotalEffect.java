package com.github.laxika.magicalvibes.model.effect;

/**
 * Replacement effect for a permanent that exiles itself when its controller would lose the
 * game, then sets that player's life total to a fixed amount.
 *
 * @param lifeTotal the life total to set after the permanent is exiled
 */
public record ReplaceControllerLossWithExileAndLifeTotalEffect(int lifeTotal) implements CardEffect {
}
