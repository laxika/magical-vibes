package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.TargetGroupCount;

/**
 * Gives the target player in the preceding target group permanent control of the permanents in
 * the following target group,
 * then the effect's controller draws one card for each permanent that changed control this way.
 */
public record TargetPlayerGainsControlOfTargetPermanentsAndDrawPerPermanentEffect(
        int permanentTargetGroupIndex) implements ControlStealingEffect, CardDrawingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new TargetGroupCount(permanentTargetGroupIndex);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
