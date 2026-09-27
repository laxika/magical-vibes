package com.github.laxika.magicalvibes.model.effect;

/**
 * Perpetually grants the supplied triggered-ability effect to the card that caused this effect to
 * trigger. The affected card identity is carried by the resolving stack entry.
 */
public record PerpetuallyGrantTriggeredAbilityEffect(CardEffect grantedEffect) implements CardEffect {
}
