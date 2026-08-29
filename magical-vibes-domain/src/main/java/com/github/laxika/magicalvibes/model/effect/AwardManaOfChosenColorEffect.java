package com.github.laxika.magicalvibes.model.effect;

/**
 * Adds one mana of the color stored by {@link ChooseColorAtResolutionEffect} and clears that
 * resolution-time choice.
 */
public record AwardManaOfChosenColorEffect() implements ManaProducingEffect {
}
