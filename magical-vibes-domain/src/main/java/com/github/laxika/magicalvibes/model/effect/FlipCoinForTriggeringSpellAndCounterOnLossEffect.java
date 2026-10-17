package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Flips a coin for the spell that caused the trigger and counters it when the caster loses.
 *
 * @param casterId the spell's caster, bound when the trigger is put on the stack so the flip still happens
 *                 for that player after the spell has left the stack ({@code null} before binding)
 */
public record FlipCoinForTriggeringSpellAndCounterOnLossEffect(UUID casterId) implements CounterSpellingEffect {

    public FlipCoinForTriggeringSpellAndCounterOnLossEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
