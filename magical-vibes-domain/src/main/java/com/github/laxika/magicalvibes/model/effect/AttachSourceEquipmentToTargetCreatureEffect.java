package com.github.laxika.magicalvibes.model.effect;

/**
 * Attaches the source equipment to a target creature.
 * Used by equipment with "When this Equipment enters, attach it to target creature you control."
 * Reads targetId as the creature to attach to, and sourcePermanentId as the equipment.
 * An optional continuation is queued as a reflexive ability only after the attachment succeeds.
 */
public record AttachSourceEquipmentToTargetCreatureEffect(CardEffect thenEffect,
                                                          boolean thenEffectOptionalTarget)
        implements CardEffect {

    public AttachSourceEquipmentToTargetCreatureEffect() {
        this(null, false);
    }

    public AttachSourceEquipmentToTargetCreatureEffect(CardEffect thenEffect) {
        this(thenEffect, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
