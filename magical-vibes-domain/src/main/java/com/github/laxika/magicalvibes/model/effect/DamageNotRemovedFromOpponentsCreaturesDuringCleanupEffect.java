package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect for "Damage isn't removed from creatures your opponents control during cleanup
 * steps."
 */
public record DamageNotRemovedFromOpponentsCreaturesDuringCleanupEffect()
        implements OpponentCreatureDamagePersistenceEffect {
}
