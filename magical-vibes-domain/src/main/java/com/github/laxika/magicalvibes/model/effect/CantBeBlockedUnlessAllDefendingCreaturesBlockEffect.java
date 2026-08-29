package com.github.laxika.magicalvibes.model.effect;

/** Attacker-side restriction that permits blocks only when every defending creature blocks it. */
public record CantBeBlockedUnlessAllDefendingCreaturesBlockEffect()
        implements BlockabilityRestrictionEffect {

    @Override
    public boolean requiresAllDefendingCreaturesToBlock() {
        return true;
    }
}
