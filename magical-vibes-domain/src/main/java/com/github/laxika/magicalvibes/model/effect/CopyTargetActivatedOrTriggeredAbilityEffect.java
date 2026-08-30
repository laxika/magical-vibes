package com.github.laxika.magicalvibes.model.effect;

/**
 * Copies target activated or triggered ability on the stack. The copy's controller may choose
 * new targets for it.
 */
public record CopyTargetActivatedOrTriggeredAbilityEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
