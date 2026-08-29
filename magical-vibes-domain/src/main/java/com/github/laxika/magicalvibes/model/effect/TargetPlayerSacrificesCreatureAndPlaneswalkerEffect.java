package com.github.laxika.magicalvibes.model.effect;

/**
 * The targeted player sacrifices a creature and a planeswalker when possible. A single permanent
 * that has both types can satisfy only one of the two sacrifices; another eligible permanent is
 * required when one exists.
 */
public record TargetPlayerSacrificesCreatureAndPlaneswalkerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
