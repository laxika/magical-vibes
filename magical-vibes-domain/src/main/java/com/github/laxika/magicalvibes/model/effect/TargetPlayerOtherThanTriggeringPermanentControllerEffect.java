package com.github.laxika.magicalvibes.model.effect;

/**
 * Marks an effect whose player target must differ from the controller of the permanent that
 * caused the trigger. Enter-trigger target collection uses the entering permanent snapshot to
 * enforce this restriction.
 */
public interface TargetPlayerOtherThanTriggeringPermanentControllerEffect extends CardEffect {
}
