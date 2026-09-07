package com.github.laxika.magicalvibes.model.effect;

/** Reveals an opponent's hand, then offers the spell's two optional choices. */
public record BindingNegotiationEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
