package com.github.laxika.magicalvibes.model.effect;

/**
 * Flips a coin for the spell that caused the trigger and counters it when the caster loses. The caster is the stack
 * entry's triggering spell's controller, so the flip still happens for that player after the spell has left the
 * stack.
 */
public record FlipCoinForTriggeringSpellAndCounterOnLossEffect() implements CounterSpellingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
