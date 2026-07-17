package com.github.laxika.magicalvibes.model.effect;

/**
 * Static blocking restriction: this creature can't block creatures with power equal to or greater than
 * its own (effective) toughness (Ironclaw Curse). Read at declare-blockers time by
 * {@code GameQueryService#findBlockDenial}, which compares the attacker's effective power against this
 * blocker's effective toughness.
 */
public record CantBlockCreaturesWithPowerGreaterOrEqualToOwnToughnessEffect() implements BlockingRestrictionEffect {

    @Override
    public boolean cantBlockCreaturesWithPowerAtLeastOwnToughness() {
        return true;
    }
}
