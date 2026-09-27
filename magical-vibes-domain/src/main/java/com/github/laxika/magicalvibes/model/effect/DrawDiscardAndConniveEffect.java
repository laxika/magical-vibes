package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.UUID;

/**
 * Makes the controller draw and then discard the given number of cards. The source or targeted
 * permanent gets a +1/+1 counter for each nonland card discarded this way.
 *
 * @param amount number of cards to draw and discard
 * @param targetPermanent whether the counter is applied to this effect's targeted permanent
 * @param useEnteringPermanentReference whether the counter is applied to the permanent that
 *                                      caused an enter-the-battlefield trigger
 * @param fixedSourcePermanentId optional non-targeting permanent reference used by effects that
 *                               capture a permanent outside the ordinary target system
 */
public record DrawDiscardAndConniveEffect(DynamicAmount amount, boolean targetPermanent,
                                          boolean useEnteringPermanentReference,
                                          UUID fixedSourcePermanentId)
        implements CardDrawingEffect, CombatDamageTriggerContextEffect {

    public DrawDiscardAndConniveEffect(DynamicAmount amount) {
        this(amount, false, false, null);
    }

    public DrawDiscardAndConniveEffect(boolean targetPermanent) {
        this(new Fixed(1), targetPermanent, false, null);
    }

    public DrawDiscardAndConniveEffect() {
        this(new Fixed(1), false, false, null);
    }

    public DrawDiscardAndConniveEffect(DynamicAmount amount, boolean targetPermanent) {
        this(amount, targetPermanent, false, null);
    }

    private DrawDiscardAndConniveEffect(DynamicAmount amount, boolean targetPermanent,
                                        boolean useEnteringPermanentReference) {
        this(amount, targetPermanent, useEnteringPermanentReference, null);
    }

    public static DrawDiscardAndConniveEffect forEnteringPermanent() {
        return new DrawDiscardAndConniveEffect(new Fixed(1), false, true);
    }

    /** Binds connive to a captured permanent reference rather than a spell target. */
    public static DrawDiscardAndConniveEffect forPermanent(UUID permanentId) {
        return new DrawDiscardAndConniveEffect(new Fixed(1), false, false, permanentId);
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return amount;
    }

    @Override
    public TargetSpec targetSpec() {
        if (fixedSourcePermanentId != null) {
            return TargetSpec.NONE;
        }
        return targetPermanent
                ? TargetSpec.benign(TargetPredicates.creature())
                : new TargetSpec(null, false, null, true, 1);
    }

    @Override
    public boolean usesEnteringPermanentReference() {
        return useEnteringPermanentReference;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return targetPermanent ? null : TriggerContext.SOURCE_SELF;
    }
}
