package com.github.laxika.magicalvibes.model.effect;

/**
 * The targeted player empties their mana pool, losing all unspent mana of every kind.
 * Targets a player via a {@code PlayerPredicateTargetFilter}. Used by Mana Short.
 */
public record TargetPlayerLosesAllUnspentManaEffect() implements CardEffect {
}
