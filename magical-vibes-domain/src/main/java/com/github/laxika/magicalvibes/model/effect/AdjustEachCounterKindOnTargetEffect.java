package com.github.laxika.magicalvibes.model.effect;

/**
 * "For each kind of counter on target permanent, put another counter of that kind on it or remove
 * one from it" (Quarry Hauler). As the ability resolves, the controller decides — independently for
 * every distinct counter kind present on the target at that moment — whether to add one more counter
 * of that kind or remove one. A target carrying no counters does nothing.
 *
 * <p>Targets any permanent ({@link TargetCategory#PERMANENT}); benign (no protection check), matching
 * the other counter-manipulation effects. The per-kind add/remove decisions are gathered through the
 * generic list-choice interaction ({@code ChoiceContext.AdjustCounterKindChoice}).</p>
 */
public record AdjustEachCounterKindOnTargetEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
