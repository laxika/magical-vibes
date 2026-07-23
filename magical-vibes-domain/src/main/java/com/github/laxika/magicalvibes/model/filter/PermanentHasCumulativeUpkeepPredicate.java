package com.github.laxika.magicalvibes.model.filter;

/**
 * Permanents that have cumulative upkeep — printed on the card or granted (temporarily or
 * persistently). Used for targeting restrictions like Balduvian Shaman's "that doesn't have
 * cumulative upkeep".
 */
public record PermanentHasCumulativeUpkeepPredicate() implements PermanentPredicate {
}
