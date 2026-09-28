package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Makes each opponent who was dealt combat damage this game by a creature with the given name lose
 * the supplied amount of life. The damage history is intentionally game-wide rather than turn-wide.
 */
public record EachOpponentDealtCombatDamageByNamedCreatureThisGameLosesLifeEffect(
        String creatureName,
        DynamicAmount amount
) implements CardEffect {
}
