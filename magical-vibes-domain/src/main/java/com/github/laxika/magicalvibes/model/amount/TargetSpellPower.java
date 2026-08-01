package com.github.laxika.magicalvibes.model.amount;

/**
 * Printed power of the targeted creature spell still on the stack at resolution, or 0 if that
 * spell has left the stack / has no power characteristic. Used by "deals damage equal to that
 * spell's power to its controller" (Essence Backlash). Pair damage before a counter so the spell
 * is still on the stack when this is evaluated.
 */
public record TargetSpellPower() implements DynamicAmount {
}
