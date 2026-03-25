package com.github.laxika.magicalvibes.model.effect;

/**
 * Sets a target player's life total to a specific value.
 * E.g. Vraska, Relic Seeker: "Target player's life total becomes 1."
 *
 * @param targetLifeTotal the life total to set for the target player
 */
public record SetTargetPlayerLifeToSpecificValueEffect(int targetLifeTotal) implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
