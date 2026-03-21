package com.github.laxika.magicalvibes.model.effect;

/**
 * Sets a target player's life total to half their starting life total, rounded down.
 * E.g. Torgaar: "target player's life total becomes half their starting life total, rounded down."
 * In a standard game (starting life = 20), this sets life to 10.
 */
public record SetTargetPlayerLifeToHalfStartingEffect() implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
