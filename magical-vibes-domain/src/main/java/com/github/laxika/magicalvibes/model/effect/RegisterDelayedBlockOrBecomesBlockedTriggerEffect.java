package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates a delayed trigger for this combat: "whenever this permanent blocks or becomes blocked by
 * a creature this combat, {@code effect}" (Goblin Flotilla). {@code effect} targets that creature
 * (e.g. {@code GrantKeywordEffect(FIRST_STRIKE, TARGET)}).
 */
public record RegisterDelayedBlockOrBecomesBlockedTriggerEffect(CardEffect effect) implements CardEffect {
}
