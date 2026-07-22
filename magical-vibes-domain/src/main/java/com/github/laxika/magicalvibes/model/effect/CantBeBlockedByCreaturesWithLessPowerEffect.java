package com.github.laxika.magicalvibes.model.effect;

/**
 * Static evasion: creatures with power less than this creature's power can't block it
 * (Shrill Howler / Howling Chorus). Compared at blocker declaration against effective powers.
 */
public record CantBeBlockedByCreaturesWithLessPowerEffect() implements BlockabilityRestrictionEffect {

    @Override
    public boolean cantBeBlockedByCreaturesWithLessPower() {
        return true;
    }
}
