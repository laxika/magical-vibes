package com.github.laxika.magicalvibes.model.effect;

/**
 * Copies the target instant or sorcery spell for each other permanent or player it could target.
 */
public record CopySpellForEachOtherPermanentOrPlayerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
