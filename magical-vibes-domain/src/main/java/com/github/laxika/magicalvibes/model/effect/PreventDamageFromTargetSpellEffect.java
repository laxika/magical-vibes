package com.github.laxika.magicalvibes.model.effect;

/**
 * Prevents all damage dealt by the targeted instant or sorcery spell for the rest of the turn.
 */
public record PreventDamageFromTargetSpellEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
