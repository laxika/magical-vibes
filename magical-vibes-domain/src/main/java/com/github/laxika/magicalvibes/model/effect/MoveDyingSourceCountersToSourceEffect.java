package com.github.laxika.magicalvibes.model.effect;

/**
 * Moves every counter a dying creature had onto the source permanent of the death trigger. The dying creature is the
 * stack entry's triggering permanent, as it last existed on the battlefield (CR 603.10a).
 */
public record MoveDyingSourceCountersToSourceEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
