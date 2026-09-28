package com.github.laxika.magicalvibes.model.effect;

/** Hosts the target creature at the source Realm. */
public record HostTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
