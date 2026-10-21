package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect for a land that becomes the basic land type chosen as it entered the battlefield.
 * The type is read from the source permanent's {@code chosenSubtype} field.
 *
 * @param inAdditionToOtherTypes {@code true} for "is the chosen type in addition to its other types"
 *                               (keeps its other land types and abilities); {@code false} for
 *                               "is the chosen type", which replaces its land types per CR 305.7
 */
public record SourceBecomesChosenBasicLandTypeEffect(boolean inAdditionToOtherTypes) implements CardEffect {

    public SourceBecomesChosenBasicLandTypeEffect() {
        this(false);
    }
}
