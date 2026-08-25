package com.github.laxika.magicalvibes.model.effect;

/** Each chosen source creature deals damage equal to its power to the chosen target creature. */
public record EachTargetCreatureDealsPowerDamageToTargetCreatureEffect(
        int sourceTargetGroup,
        int victimTargetGroup
) implements CardEffect {

    public EachTargetCreatureDealsPowerDamageToTargetCreatureEffect() {
        this(1, 0);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

}
