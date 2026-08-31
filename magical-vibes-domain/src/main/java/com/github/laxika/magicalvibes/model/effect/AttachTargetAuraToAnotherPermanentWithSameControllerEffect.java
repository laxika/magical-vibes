package com.github.laxika.magicalvibes.model.effect;

/**
 * Moves the target Aura to another permanent controlled by the current host's controller. The new
 * permanent is chosen by the ability's controller while this effect resolves.
 */
public record AttachTargetAuraToAnotherPermanentWithSameControllerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
