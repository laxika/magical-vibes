package com.github.laxika.magicalvibes.model.effect;

/**
 * Phases out all creatures and keeps them phased out until the source permanent leaves the
 * battlefield. The source receives one time counter for each creature that successfully phases out.
 */
public record PhaseOutAllCreaturesUntilSourceLeavesEffect() implements CardEffect {
}
