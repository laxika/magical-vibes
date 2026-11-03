package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for a trigger that fires only when its controller puts a counter on a creature they
 * control. Don't author it: the triggers {@code Triggers.youPutCountersOnCreatureYouControl()} and
 * {@code Triggers.youPutPlusOneCountersOnThis()} say this, and their legacy view
 * ({@code TriggeredAbility.legacyEffects()}) gives it to the counter collectors that still judge it.
 */
public record YouPutCounterOnControlledCreatureTriggerEffect(CardEffect wrapped) implements CardEffect {
}
