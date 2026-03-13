package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker interface for replacement effects (MTG rule 614).
 * <p>
 * Replacement effects modify how an event happens rather than triggering after it.
 * They never go on the stack and can't be responded to. Examples include
 * "enters the battlefield tapped", "enters with N counters", and
 * "enters as a copy of".
 * <p>
 * Effects registered in {@code EffectSlot.ON_ENTER_BATTLEFIELD} that implement
 * this interface are automatically excluded from the triggered-ability pipeline
 * and handled during the permanent entry process instead.
 */
public interface ReplacementEffect extends CardEffect {
}
