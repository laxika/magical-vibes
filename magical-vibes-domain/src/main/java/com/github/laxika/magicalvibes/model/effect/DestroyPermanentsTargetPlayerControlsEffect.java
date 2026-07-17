package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Destroys every permanent the targeted player controls that matches {@code filter}
 * (e.g. all their lands). Targets a player; the destruction respects indestructible and
 * regeneration. Used by Ajani Vengeant's ultimate ("Destroy all lands target player controls").
 *
 * @param filter narrows which of the target player's permanents are destroyed
 */
public record DestroyPermanentsTargetPlayerControlsEffect(
        PermanentPredicate filter
) implements BoardWipeEffect {

    /** Destroys every matching permanent the target player controls — always a sweep. */
    @Override
    public boolean sweepsBoard() {
        return true;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER);
    }
}
