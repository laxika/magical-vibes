package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller sacrifices a creature with the least effective toughness among creatures they
 * control. If several creatures are tied, the controller chooses one of them.
 */
public record SacrificeCreatureWithLeastToughnessEffect() implements CardEffect {
}
