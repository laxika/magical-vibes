package com.github.laxika.magicalvibes.model.effect;

/**
 * Continuous boost for each legendary creature the source's controller controls, scaled by the
 * number of other legendary creatures that player controls.
 */
public record BoostLegendaryCreaturesByOtherLegendaryCreaturesEffect(
        int powerPerCreature,
        int toughnessPerCreature
) implements CardEffect {
}
