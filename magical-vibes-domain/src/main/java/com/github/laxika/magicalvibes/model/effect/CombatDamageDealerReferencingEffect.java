package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for an ally combat-damage trigger whose effect needs the creature that dealt the damage.
 * The combat collector preserves that creature as the trigger's non-targeting event source while
 * retaining the permanent carrying the ability as the stack entry source.
 */
public interface CombatDamageDealerReferencingEffect extends CardEffect {
}
