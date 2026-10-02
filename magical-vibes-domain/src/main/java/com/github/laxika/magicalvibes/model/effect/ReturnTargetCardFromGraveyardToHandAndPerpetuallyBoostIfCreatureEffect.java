package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;

/** Returns the targeted graveyard card to its owner's hand and boosts it if it is a creature card. */
public record ReturnTargetCardFromGraveyardToHandAndPerpetuallyBoostIfCreatureEffect(
        int powerBoost, int toughnessBoost) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCard(GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
