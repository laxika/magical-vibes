package com.github.laxika.magicalvibes.model.effect;

public record CantBeBlockedEffect() implements BlockabilityRestrictionEffect {

    @Override
    public boolean cantBeBlocked() {
        return true;
    }
}
