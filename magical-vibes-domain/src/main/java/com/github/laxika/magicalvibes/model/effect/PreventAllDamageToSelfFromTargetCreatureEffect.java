package com.github.laxika.magicalvibes.model.effect;

/**
 * Prevents all damage that would be dealt to the source permanent by the target creature this turn.
 */
public record PreventAllDamageToSelfFromTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
