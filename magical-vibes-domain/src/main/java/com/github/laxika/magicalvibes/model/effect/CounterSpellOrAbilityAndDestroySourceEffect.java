package com.github.laxika.magicalvibes.model.effect;

/**
 * Counters a target spell or ability and destroys the source permanent when the target was a
 * permanent's ability.
 */
public record CounterSpellOrAbilityAndDestroySourceEffect() implements CounterSpellingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
