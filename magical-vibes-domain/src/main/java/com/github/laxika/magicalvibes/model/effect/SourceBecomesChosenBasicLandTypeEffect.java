package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect for a land that becomes the basic land type chosen as it entered the battlefield.
 * The type is read from the source permanent's {@code chosenSubtype} field.
 */
public record SourceBecomesChosenBasicLandTypeEffect() implements CardEffect {
}
