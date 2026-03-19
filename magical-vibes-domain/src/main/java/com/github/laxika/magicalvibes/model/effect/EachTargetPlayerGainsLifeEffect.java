package com.github.laxika.magicalvibes.model.effect;

/**
 * Each targeted player gains N life. Uses {@code entry.getTargetIds()} for the target list.
 * Pair with {@code setMinTargets(0)} and {@code setMaxTargets(99)} for "any number of target players".
 * Used by Hunters' Feast.
 */
public record EachTargetPlayerGainsLifeEffect(int amount) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
