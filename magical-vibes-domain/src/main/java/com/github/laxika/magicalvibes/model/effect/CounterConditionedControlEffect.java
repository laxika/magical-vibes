package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * A control-changing effect that remains active only while its affected permanent has the
 * specified counter. The control layer is removed permanently when the condition stops holding.
 */
public interface CounterConditionedControlEffect extends ControlStealingEffect {

    /** The counter that must remain on the affected permanent. */
    CounterType counterType();
}
