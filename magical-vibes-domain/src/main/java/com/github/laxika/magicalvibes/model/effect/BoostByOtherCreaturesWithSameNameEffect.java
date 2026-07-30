package com.github.laxika.magicalvibes.model.effect;

/**
 * Static self-boost of +{@code powerPerCreature}/+{@code toughnessPerCreature} for each other creature
 * with the same name as the source. When {@code onlyControlled} is set, only creatures controlled by the
 * source's controller are counted ("each other creature you control named ..."); otherwise every such
 * creature on the battlefield counts regardless of controller (Relentless Rats).
 */
public record BoostByOtherCreaturesWithSameNameEffect(int powerPerCreature, int toughnessPerCreature,
                                                      boolean onlyControlled) implements CardEffect {

    public BoostByOtherCreaturesWithSameNameEffect(int powerPerCreature, int toughnessPerCreature) {
        this(powerPerCreature, toughnessPerCreature, false);
    }
}
