package com.github.laxika.magicalvibes.model.effect;

/**
 * Marks an effect whose player target must differ from the controller of the permanent that
 * caused the trigger. Enter-trigger target collection enforces this restriction with the controller of the
 * triggering permanent it is given.
 */
public interface TargetPlayerOtherThanTriggeringPermanentControllerEffect extends CardEffect {
}
