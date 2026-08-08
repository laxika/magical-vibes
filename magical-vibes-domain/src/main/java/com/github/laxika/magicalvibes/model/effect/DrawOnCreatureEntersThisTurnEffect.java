package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a turn-scoped delayed trigger: whenever any creature enters the battlefield this turn,
 * the spell's controller may draw a card.
 */
public record DrawOnCreatureEntersThisTurnEffect() implements CardEffect {
}
