package com.github.laxika.magicalvibes.model.effect;

/** Lets the controller choose any number of creatures in the bound target group to phase out. */
public record PhaseOutChosenTargetCreaturesEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
