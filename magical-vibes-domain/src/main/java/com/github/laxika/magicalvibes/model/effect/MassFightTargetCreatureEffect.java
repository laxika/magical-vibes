package com.github.laxika.magicalvibes.model.effect;

/**
 * Target creature an opponent controls deals damage equal to its power to each other creature
 * that player controls, then each of those creatures deals damage equal to its power to that creature.
 * Each creature is the source of its own damage (relevant for protection, deathtouch, lifelink, etc.).
 * State-based actions are not checked between the two damage steps.
 * Used by Alpha Brawl.
 */
public record MassFightTargetCreatureEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
