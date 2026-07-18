package com.github.laxika.magicalvibes.model.effect;

/**
 * STATIC-slot companion to {@link TargetLandBecomesForestUntilSourceLeavesEffect}: every land whose
 * id the source permanent has recorded in {@code forestedLandIds} becomes a Forest (a CR 305.7
 * basic-land-type replacement — it loses its other land types/abilities and taps for {G}). Applied
 * in layer 4 by {@code TrackedLandsBecomeForestEffectHandler}. The grant ends the moment the source
 * leaves the battlefield (its static effects are no longer collected), matching Gaea's Liege's
 * "until this creature leaves the battlefield" duration.
 */
public record TrackedLandsBecomeForestEffect() implements CardEffect {
}
