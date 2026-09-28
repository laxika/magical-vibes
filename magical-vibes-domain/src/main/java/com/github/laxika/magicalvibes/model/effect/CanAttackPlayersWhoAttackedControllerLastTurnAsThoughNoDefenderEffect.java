package com.github.laxika.magicalvibes.model.effect;

/**
 * Static permission for a creature with defender to attack players who attacked its controller
 * during those players' most recently completed turns.
 */
public record CanAttackPlayersWhoAttackedControllerLastTurnAsThoughNoDefenderEffect()
        implements CardEffect {
}
