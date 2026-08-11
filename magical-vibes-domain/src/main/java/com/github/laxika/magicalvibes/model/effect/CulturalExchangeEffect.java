package com.github.laxika.magicalvibes.model.effect;

/**
 * At resolution, the spell's controller chooses equal numbers of creatures controlled by two
 * target players. Those creatures exchange control permanently.
 */
public record CulturalExchangeEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(TargetPredicates.player(), false, null, false, 2);
    }
}
