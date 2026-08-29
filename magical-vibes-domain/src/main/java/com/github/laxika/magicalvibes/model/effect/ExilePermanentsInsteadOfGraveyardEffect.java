package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static replacement effect that exiles permanents instead of letting them enter a graveyard.
 * The replacement only applies to permanents leaving the battlefield, not to permanent cards in
 * other zones. A non-null {@code filter} narrows which permanents are replaced; the optional
 * source exclusion and source tracking support effects such as Void Maw.
 *
 * @param filter         permanent filter, or {@code null} for every permanent
 * @param excludeSource  whether the source permanent itself is excluded
 * @param trackWithSource whether exiled cards are tracked with the source permanent
 */
public record ExilePermanentsInsteadOfGraveyardEffect(
        PermanentPredicate filter,
        boolean excludeSource,
        boolean trackWithSource
) implements CardEffect {

    /** The global replacement used by Samurai of the Pale Curtain. */
    public ExilePermanentsInsteadOfGraveyardEffect() {
        this(null, false, false);
    }
}
