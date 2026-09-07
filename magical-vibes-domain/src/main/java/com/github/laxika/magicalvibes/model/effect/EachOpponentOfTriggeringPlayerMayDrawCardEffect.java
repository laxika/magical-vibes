package com.github.laxika.magicalvibes.model.effect;

/**
 * Spell-cast trigger payload that offers one card draw to each opponent of the player who cast
 * the triggering spell.
 */
public record EachOpponentOfTriggeringPlayerMayDrawCardEffect() implements CardEffect {
}
