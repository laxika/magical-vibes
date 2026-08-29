package com.github.laxika.magicalvibes.model.effect;

/** The controller investigates once for each creature controlled by the target players. */
public record InvestigateForEachTargetPlayerCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
