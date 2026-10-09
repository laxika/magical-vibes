package com.github.laxika.magicalvibes.model.effect;

/**
 * Moves the target Aura to a different permanent of the type of its current host. The new
 * permanent is chosen by the spell's controller while this effect resolves.
 */
public record AttachTargetAuraToAnotherPermanentOfSameTypeEffect(boolean creaturesOnly) implements CardEffect {

    public AttachTargetAuraToAnotherPermanentOfSameTypeEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
