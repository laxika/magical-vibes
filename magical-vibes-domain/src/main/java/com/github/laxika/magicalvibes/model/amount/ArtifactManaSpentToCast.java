package com.github.laxika.magicalvibes.model.amount;

/**
 * The amount of mana from artifact sources (Treasures included) spent to cast the spell this amount
 * belongs to; 0 for a permanent that wasn't cast. On an entering-permanent entry replacement it is
 * the mana spent to cast the entering permanent (Coin of Mastery).
 */
public record ArtifactManaSpentToCast() implements DynamicAmount {
}
