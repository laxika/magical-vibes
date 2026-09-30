package com.github.laxika.magicalvibes.model.effect;

/** Conjures a duplicate of the targeted spell into the controller's hand. */
public record ConjureDuplicateOfTargetSpellIntoHandEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
