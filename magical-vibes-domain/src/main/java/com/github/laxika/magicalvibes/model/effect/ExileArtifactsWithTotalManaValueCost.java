package com.github.laxika.magicalvibes.model.effect;

/**
 * Activation cost that exiles one or more other artifacts the activating player controls.
 * The mana values of the exiled artifacts are added together for the ability's X value.
 */
public record ExileArtifactsWithTotalManaValueCost() implements CostEffect {
}
