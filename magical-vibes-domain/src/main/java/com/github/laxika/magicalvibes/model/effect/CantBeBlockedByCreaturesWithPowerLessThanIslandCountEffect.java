package com.github.laxika.magicalvibes.model.effect;

/**
 * Static evasion: creatures with power less than the number of Islands controlled by this
 * creature's controller can't block it (Kraken of the Straits). The threshold is checked at
 * blocker declaration against current effective powers and the current Island count.
 */
public record CantBeBlockedByCreaturesWithPowerLessThanIslandCountEffect()
        implements BlockabilityRestrictionEffect {

    @Override
    public boolean cantBeBlockedByCreaturesWithPowerLessThanIslandCount() {
        return true;
    }
}
