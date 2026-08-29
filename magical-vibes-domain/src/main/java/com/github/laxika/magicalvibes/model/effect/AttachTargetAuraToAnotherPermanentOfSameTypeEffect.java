package com.github.laxika.magicalvibes.model.effect;

/**
 * Moves the target Aura to a different permanent of the type of its current host. The new
 * permanent is chosen by the spell's controller while this effect resolves.
 */
public record AttachTargetAuraToAnotherPermanentOfSameTypeEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
