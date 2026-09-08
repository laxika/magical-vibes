package com.github.laxika.magicalvibes.model.effect;

/**
 * Additional cast cost that requires the caster to choose a creature type.
 * The choice itself consumes no resource and is carried by the spell's stack entry.
 */
public record ChooseCreatureTypeCost() implements CostEffect {
}
