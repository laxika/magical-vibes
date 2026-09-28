package com.github.laxika.magicalvibes.model.effect;

/** This creature can't be blocked as long as the defending player has a rad counter. */
public record CantBeBlockedIfDefenderHasRadCountersEffect() implements BlockabilityRestrictionEffect {

    @Override
    public boolean unblockableIfDefenderHasRadCounters() {
        return true;
    }
}
