package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect for noncreature sources controlled by the effect's controller dealing
 * damage to a creature, battle, or opponent.
 */
public record AdditionalDamageFromNoncreatureSourcesEffect(int amount)
        implements NoncreatureSourceDamageBonusEffect {
}
