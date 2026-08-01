package com.github.laxika.magicalvibes.model.effect;

/**
 * Global static: each player skips their untap step while any permanent carrying this effect is on
 * the battlefield (Sands of Time). Unlike {@link MatchingPermanentsDoesntUntapEffect}, this skips
 * the entire step — including the CR 502.1 phasing turn-based action — so phased-out permanents do
 * not phase in and permanents with phasing do not phase out. Summoning sickness still clears.
 * Read by {@code UntapStepService#playersSkipUntapStepApplies}. Works while the source is tapped.
 */
public record PlayersSkipUntapStepEffect() implements CardEffect {
}
