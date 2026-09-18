package com.github.laxika.magicalvibes.model.effect;

/**
 * Static evasion effect: this creature can't be blocked as long as the defending player
 * controls the most creatures or is tied for the most.
 */
public record CantBeBlockedIfDefenderControlsMostCreaturesOrTiedEffect()
        implements BlockabilityRestrictionEffect {

    @Override
    public boolean unblockableIfDefenderControlsMostCreaturesOrTied() {
        return true;
    }
}
