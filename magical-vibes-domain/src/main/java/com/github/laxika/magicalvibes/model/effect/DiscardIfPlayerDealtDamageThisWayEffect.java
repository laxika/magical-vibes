package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes each player who was actually dealt damage by an earlier effect in this stack-entry
 * resolution discard one card. Fully prevented damage and damage dealt only to permanents do not
 * qualify.
 */
public record DiscardIfPlayerDealtDamageThisWayEffect() implements CardEffect {
}
