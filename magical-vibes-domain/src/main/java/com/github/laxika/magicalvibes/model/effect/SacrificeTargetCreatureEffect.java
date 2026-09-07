package com.github.laxika.magicalvibes.model.effect;

/** Sacrifices the creature chosen for the configured target group. */
public record SacrificeTargetCreatureEffect(int targetGroup) implements CardEffect {

    public SacrificeTargetCreatureEffect() {
        this(0);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
