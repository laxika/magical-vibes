package com.github.laxika.magicalvibes.model.effect;

/**
 * Static evasion restriction: this creature can't be blocked unless it is attacking its owner or
 * a permanent controlled by its owner.
 */
public record CantBeBlockedUnlessAttackingOwnerOrOwnerControlledPermanentEffect()
        implements BlockabilityRestrictionEffect {

    @Override
    public boolean unblockableUnlessAttackingOwnerOrOwnerControlledPermanent() {
        return true;
    }
}
