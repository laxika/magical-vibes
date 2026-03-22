package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the source permanent become an artifact creature until end of turn,
 * using its own printed power and toughness. Used by the Crew keyword ability
 * on Vehicle artifacts.
 */
public record AnimateSelfAsCreatureEffect() implements CardEffect {

    @Override
    public boolean isSelfTargeting() { return true; }
}
