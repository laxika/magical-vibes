package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for a trigger that fires only when its controller puts a counter on a creature they
 * control.
 */
public record YouPutCounterOnControlledCreatureTriggerEffect(CardEffect wrapped) implements CardEffect {
}
