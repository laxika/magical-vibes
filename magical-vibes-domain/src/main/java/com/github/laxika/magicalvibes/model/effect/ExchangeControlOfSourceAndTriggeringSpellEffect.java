package com.github.laxika.magicalvibes.model.effect;

/**
 * Exchanges control of the source permanent and the spell that caused the source's spell-cast
 * trigger. The triggering spell is carried by {@code StackEntry.triggeringCardId}; this effect
 * does not target it.
 */
public record ExchangeControlOfSourceAndTriggeringSpellEffect() implements CardEffect {
}
