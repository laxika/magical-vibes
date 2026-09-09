package com.github.laxika.magicalvibes.model.effect;

/** Exiles a target creature and its attached Auras until the source permanent leaves. */
public record ExileTargetCreatureAndAurasUntilSourceLeavesEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
