package com.github.laxika.magicalvibes.model.effect;

/** Attacker-side restriction requiring the defending player to control a shared-type creature group. */
public record CantBeBlockedUnlessDefenderControlsCreaturesSharingCreatureTypeEffect(int minimum)
        implements BlockabilityRestrictionEffect {

    public CantBeBlockedUnlessDefenderControlsCreaturesSharingCreatureTypeEffect {
        if (minimum < 1) {
            throw new IllegalArgumentException("minimum must be positive");
        }
    }

    @Override
    public Integer defenderControlsCreaturesSharingTypeMinimum() {
        return minimum;
    }
}
