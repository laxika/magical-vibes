package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;

/**
 * Puts a target card from any graveyard on its owner's library, with the spell controller
 * choosing whether it goes on top or bottom.
 */
public record PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect(Destination destination)
        implements CardEffect {

    public PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect() {
        this(Destination.CHOOSE);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCard(GraveyardSearchScope.ALL_GRAVEYARDS));
    }

    public enum Destination {
        CHOOSE,
        TOP,
        BOTTOM
    }
}
