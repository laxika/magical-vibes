package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

/**
 * Static restriction: permanents controlled by this effect's source controller that match
 * {@code filter} can't transform (e.g. Immerwolf's "Non-Human Werewolves you control can't
 * transform"). Wrapped in an {@link AttachedPermanentRestrictionEffect} it is the Aura's or
 * Equipment's restriction on the permanent it is attached to, which can't transform while it matches
 * {@code filter} ({@link #PreventTransformEffect()}: "enchanted creature can't transform", Bound by
 * Moonsilver).
 *
 * <p>The restriction is consulted by the transform resolution logic before a permanent flips
 * faces. The {@code filter} is evaluated against the permanent that is about to transform, using
 * its current face, so a card moving from a non-matching face to a matching one is unaffected
 * while a permanent already on a matching face is locked in place.
 */
public record PreventTransformEffect(PermanentPredicate filter) implements CardEffect {

    /** Any permanent: as an attached restriction, "enchanted / equipped creature can't transform". */
    public PreventTransformEffect() {
        this(new PermanentTruePredicate());
    }
}
