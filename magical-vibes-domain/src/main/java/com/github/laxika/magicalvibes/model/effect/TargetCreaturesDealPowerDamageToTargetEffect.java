package com.github.laxika.magicalvibes.model.effect;

/** Each creature chosen for one target group deals its power as damage to a creature chosen for another group. */
public record TargetCreaturesDealPowerDamageToTargetEffect(int sourceTargetGroup, int victimTargetGroup)
        implements CardEffect {

    public TargetCreaturesDealPowerDamageToTargetEffect() {
        this(0, 1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
