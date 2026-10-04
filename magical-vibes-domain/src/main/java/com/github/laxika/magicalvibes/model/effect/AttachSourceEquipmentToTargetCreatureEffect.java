package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Attaches the source equipment to a target creature.
 * Used by equipment with "When this Equipment enters, attach it to target creature you control."
 * Reads targetId as the creature to attach to, and sourcePermanentId as the equipment.
 * An optional continuation is queued as a reflexive ability only after the attachment succeeds.
 */
public record AttachSourceEquipmentToTargetCreatureEffect(CardEffect thenEffect,
                                                          boolean thenEffectOptionalTarget,
                                                          PermanentPredicate targetPredicate,
                                                          boolean useTriggeringPermanent)
        implements CardEffect {

    public AttachSourceEquipmentToTargetCreatureEffect(CardEffect thenEffect,
                                                        boolean thenEffectOptionalTarget,
                                                        PermanentPredicate targetPredicate) {
        this(thenEffect, thenEffectOptionalTarget, targetPredicate, false);
    }

    public AttachSourceEquipmentToTargetCreatureEffect() {
        this(null, false, null);
    }

    public AttachSourceEquipmentToTargetCreatureEffect(CardEffect thenEffect) {
        this(thenEffect, false, null);
    }

    public AttachSourceEquipmentToTargetCreatureEffect(CardEffect thenEffect,
                                                       boolean thenEffectOptionalTarget) {
        this(thenEffect, thenEffectOptionalTarget, null);
    }

    public static AttachSourceEquipmentToTargetCreatureEffect forCreatureYouControl() {
        return new AttachSourceEquipmentToTargetCreatureEffect(
                null, false, new PermanentControlledBySourceControllerPredicate());
    }

    /** Attaches to the triggering creature as a non-targeting reference. */
    public static AttachSourceEquipmentToTargetCreatureEffect forTriggeringCreature() {
        return new AttachSourceEquipmentToTargetCreatureEffect(null, false, null, true);
    }

    @Override
    public TargetSpec targetSpec() {
        if (useTriggeringPermanent) return TargetSpec.NONE;
        return targetPredicate == null
                ? TargetSpec.benign(TargetPredicates.permanent())
                : TargetSpec.benign(TargetPredicates.creature(), targetPredicate);
    }
}
