package com.github.laxika.magicalvibes.model.effect;

/**
 * Copies target activated or triggered ability on the stack. The copy's controller may choose
 * new targets for it. The effect's target filter must restrict targets to the intended ability
 * types and controller.
 */
public record CopyTargetAbilityEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
