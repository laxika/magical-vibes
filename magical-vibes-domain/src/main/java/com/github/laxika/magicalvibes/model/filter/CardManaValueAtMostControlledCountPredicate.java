package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches cards whose mana value is at most the number of matching permanents controlled by the
 * perspective player.
 */
public record CardManaValueAtMostControlledCountPredicate(PermanentPredicate countFilter)
        implements CardPredicate {
}
