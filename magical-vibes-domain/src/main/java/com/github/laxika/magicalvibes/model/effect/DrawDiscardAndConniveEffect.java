package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Makes the controller draw a card, then discard a card. If the discarded card is
 * nonland, the source or targeted permanent gets a +1/+1 counter.
 *
 * @param targetPermanent whether the counter is applied to this effect's targeted permanent
 */
public record DrawDiscardAndConniveEffect(boolean targetPermanent)
        implements CardDrawingEffect, CombatDamageTriggerContextEffect {

    public DrawDiscardAndConniveEffect() {
        this(false);
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetPermanent ? TargetSpec.benign(TargetPredicates.creature()) : TargetSpec.NONE;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return targetPermanent ? null : TriggerContext.SOURCE_SELF;
    }
}
