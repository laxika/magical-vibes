package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

/**
 * A delayed triggered ability waiting for its controller to next cast a creature spell this turn
 * ("When you next cast a creature spell this turn, ..."). When that spell is cast the effect is put
 * on the stack above it as a triggered ability of {@code source}.
 *
 * @param source the card whose ability created the delayed trigger
 * @param effect the effect that resolves when the trigger fires
 */
public record DelayedSpellCastTrigger(Card source, CardEffect effect) {
}
