package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target player loses all counters. That player can't get counters for as long as this creature
 * remains on the battlefield." (Suncleanser's second mode; the "opponent" restriction is the mode's
 * own target filter, not part of this effect.)
 *
 * <p>Poison is the only player counter the engine tracks, so "loses all counters" clears
 * {@code GameData.playerPoisonCounters}. The lock is recorded in
 * {@code GameData.countersLockedPlayersWhileSourceOnBattlefield} keyed by the source permanent and
 * read back by {@code GameQueryService.canPlayerGetPoisonCounters}; it ends on its own when the
 * source leaves the battlefield.</p>
 */
public record RemoveAllCountersAndLockPlayerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
