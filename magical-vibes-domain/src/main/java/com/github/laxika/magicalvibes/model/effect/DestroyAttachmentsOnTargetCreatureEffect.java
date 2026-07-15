package com.github.laxika.magicalvibes.model.effect;

/** Destroy the Auras and/or Equipment attached to the target creature. */
public record DestroyAttachmentsOnTargetCreatureEffect(boolean auras, boolean equipment) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.CREATURE);
    }
}
