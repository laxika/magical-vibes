package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger descriptor and resolved effect for countering a spell cast without spending colored
 * mana. The spell-cast trigger collector checks the payment snapshot when the ability triggers and
 * targets that triggering spell.
 */
public record CounterSpellIfNoColoredManaSpentEffect() implements CounterSpellingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
