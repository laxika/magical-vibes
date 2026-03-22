package com.github.laxika.magicalvibes.model.effect;

/**
 * ETB replacement effect: "This creature enters with a +1/+1 counter on it
 * for each creature that died this turn."
 * Counts all creature deaths (including tokens) across all players this turn.
 * (e.g. Bloodcrazed Paladin)
 */
public record EnterWithPlusOnePlusOneCountersPerCreatureDeathsThisTurnEffect() implements ReplacementEffect {
}
