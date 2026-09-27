package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Zabaz, the Glimmerwasp's replacement for +1/+1 counters placed by modular abilities. */
public record AddOnePlusOneCountersToModularAbilityEffect() implements CounterReplacementEffect {

    @Override
    public int replace(CounterType counterType, int count) {
        return counterType == CounterType.PLUS_ONE_PLUS_ONE && count > 0 ? count + 1 : count;
    }

    @Override
    public boolean appliesTo(CounterType counterType, boolean affectedPermanentIsCreature) {
        return counterType == CounterType.PLUS_ONE_PLUS_ONE && affectedPermanentIsCreature;
    }

    @Override
    public boolean appliesOnlyToModularAbility() {
        return true;
    }
}
