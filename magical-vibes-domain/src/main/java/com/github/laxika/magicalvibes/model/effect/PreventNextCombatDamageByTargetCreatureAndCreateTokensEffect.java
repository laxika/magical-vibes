package com.github.laxika.magicalvibes.model.effect;

/**
 * Prevents the next combat damage the target creature would deal to one or more players this
 * combat, then creates one token for each damage prevented.
 */
public record PreventNextCombatDamageByTargetCreatureAndCreateTokensEffect(CreateTokenEffect token)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
