package com.github.laxika.magicalvibes.model.effect;

/** Destroy the Auras and/or Equipment attached to the target permanent. */
public record DestroyAttachmentsOnTargetCreatureEffect(
        boolean auras,
        boolean equipment,
        TargetCategory targetCategory
) implements CardEffect {

    public DestroyAttachmentsOnTargetCreatureEffect(boolean auras, boolean equipment) {
        this(auras, equipment, TargetCategory.CREATURE);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(targetCategory);
    }
}
