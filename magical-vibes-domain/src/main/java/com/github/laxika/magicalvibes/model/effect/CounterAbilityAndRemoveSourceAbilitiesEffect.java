package com.github.laxika.magicalvibes.model.effect;

/**
 * Counters a target activated or triggered ability and removes all abilities from its source
 * permanent for as long as this effect's source permanent remains on the battlefield.
 */
public record CounterAbilityAndRemoveSourceAbilitiesEffect() implements CounterSpellingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
