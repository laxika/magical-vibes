package com.github.laxika.magicalvibes.model.amount;

/**
 * Power of the targeted creature spell at resolution (Essence Backlash): read from the spell while it
 * is on the stack, or from the copy remembered when an earlier effect of the same resolution countered
 * it. Characteristic-defining P/T is evaluated. 0 if neither is available or the spell has no power.
 */
public record TargetSpellPower() implements DynamicAmount {
}
