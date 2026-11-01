package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.DrawReplacementKind;

/**
 * Capability marker for a static effect that may replace its controller's draw with an alternative.
 *
 * <p>Draw replacement markers are consumed by {@code DrawService}, where the affected player is
 * offered the replacement choice one draw at a time. The ones that apply only in their controller's
 * draw step ({@link #drawStepOnly()}) are offered by {@code StepTriggerService.handleDrawStep} for the
 * turn-based draw.
 */
public interface MaySkipDrawReplacementEffect extends CardEffect {

    DrawReplacementKind replacementKind();

    /** Whether the replacement applies only during its controller's draw step (Island Sanctuary, Fasting). */
    default boolean drawStepOnly() {
        return false;
    }
}
