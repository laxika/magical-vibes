package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Makes the controller draw and then discard the given number of cards. The source or targeted
 * permanent gets a +1/+1 counter for each nonland card discarded this way.
 *
 * @param amount number of cards to draw and discard
 * @param targetPermanent whether the counter is applied to this effect's targeted permanent
 * @param useEnteringPermanentReference whether the counter is applied to the permanent that
 *                                      caused an enter-the-battlefield trigger
 */
public record DrawDiscardAndConniveEffect(DynamicAmount amount, boolean targetPermanent, boolean useEnteringPermanentReference)
        implements CardDrawingEffect, CombatDamageTriggerContextEffect {

    public DrawDiscardAndConniveEffect(DynamicAmount amount) {
        this(amount, false);
    }

    public DrawDiscardAndConniveEffect(boolean targetPermanent) {
        this(new Fixed(1), targetPermanent);
    }

    public DrawDiscardAndConniveEffect() {
        this(new Fixed(1), false);
    }

    public DrawDiscardAndConniveEffect(DynamicAmount amount, boolean targetPermanent) {
        this(amount, targetPermanent, false);
    }

    public static DrawDiscardAndConniveEffect forEnteringPermanent() {
        return new DrawDiscardAndConniveEffect(new Fixed(1), false, true);
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return amount;
    }

    @Override
    public TargetSpec targetSpec() {
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
