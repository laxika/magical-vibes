package com.github.laxika.magicalvibes.model.effect;

/** Destroy the Auras and/or Equipment attached to the target permanent. */
public record DestroyAttachmentsOnTargetCreatureEffect(
        boolean auras,
        boolean equipment,
        TargetPredicate declaredTarget
) implements CardEffect, CombatOpponentReferencingEffect {

    public DestroyAttachmentsOnTargetCreatureEffect(boolean auras, boolean equipment) {
        this(auras, equipment, TargetPredicates.creature());
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(declaredTarget);
    }
}
