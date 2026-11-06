package com.github.laxika.magicalvibes.model.effect;

/**
 * Gains control of the target creature for as long as "that player" — the stack entry's triggering player, who became
 * the monarch — remains the monarch.
 */
public record GainControlOfTargetCreatureWhileMonarchEffect() implements MonarchBoundControlEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
