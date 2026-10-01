package com.github.laxika.magicalvibes.model.effect;

/** Exchanges control of a target noncreature spell and a target creature. */
public record ExchangeControlOfTargetSpellAndCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
