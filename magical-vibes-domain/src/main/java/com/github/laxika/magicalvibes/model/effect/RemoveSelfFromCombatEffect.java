package com.github.laxika.magicalvibes.model.effect;

/** Removes the source permanent from combat without targeting it. */
public record RemoveSelfFromCombatEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
