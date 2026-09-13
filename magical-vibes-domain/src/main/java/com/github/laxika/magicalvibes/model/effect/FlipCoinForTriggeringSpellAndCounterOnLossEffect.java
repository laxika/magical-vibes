package com.github.laxika.magicalvibes.model.effect;

/** Flips a coin for the spell that caused the trigger and counters it when the caster loses. */
public record FlipCoinForTriggeringSpellAndCounterOnLossEffect() implements CounterSpellingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
