package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * A spell on the stack that becomes {@code color} until end of turn (Blind Seer): the payload of a floating effect on
 * the spell's card ({@code FloatingContinuousEffect#onCard}). When the spell resolves into a permanent, the effect
 * is made one on that permanent (CR 400.7a); otherwise it ends at the cleanup step.
 */
public record SpellColorUntilEndOfTurnEffect(CardColor color) implements CardEffect {
}
