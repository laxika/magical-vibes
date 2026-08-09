package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * As this permanent enters, sacrifice all permanents you control matching {@code filter}.
 * The entering permanent is not on the battlefield yet and therefore cannot be sacrificed by
 * this effect.
 */
public record SacrificeAllPermanentsAsEntersEffect(PermanentPredicate filter) implements ReplacementEffect {
}
