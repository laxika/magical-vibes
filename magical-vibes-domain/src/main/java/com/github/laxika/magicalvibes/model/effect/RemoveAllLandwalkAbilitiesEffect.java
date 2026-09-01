package com.github.laxika.magicalvibes.model.effect;

/**
 * Causes the targeted creature to lose all landwalk abilities until end of turn.
 *
 * <p>The normal landwalk keywords are removed by the effect handler, while the handler also
 * suppresses the engine's predicate-backed snow-landwalk representation for the same duration.
 */
public record RemoveAllLandwalkAbilitiesEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
