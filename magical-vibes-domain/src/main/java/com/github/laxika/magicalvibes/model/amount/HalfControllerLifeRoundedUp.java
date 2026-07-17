package com.github.laxika.magicalvibes.model.amount;

/**
 * Half the controller's current life total, rounded up (e.g. "loses half their life, rounded up" —
 * Personal Incarnation's death trigger). Computed as {@code (life + 1) / 2} against the controller of
 * the spell/ability the amount belongs to.
 */
public record HalfControllerLifeRoundedUp() implements DynamicAmount {
}
