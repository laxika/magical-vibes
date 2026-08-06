package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedule the source permanent to be destroyed at the beginning of the next end step
 * (e.g. Crazed Armodon's "Destroy this creature at the beginning of the next end step").
 * Operates on the source, so it carries no target. Regular destruction, so indestructible
 * and regeneration apply — unlike {@link SacrificeSelfAtEndStepEffect}.
 */
public record DestroySelfAtEndStepEffect() implements CardEffect {
}
