package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "As this enters, sacrifice all [filter] you control" (Overlaid Terrain) and "if this would enter, instead sacrifice
 * each other permanent named [this] you control, then put it onto the battlefield" (Sheltered Valley, with
 * {@code PermanentHasSameNameAsSourcePredicate}): as this permanent enters, its controller sacrifices every permanent
 * they control matching {@code filter}, judged with this permanent as the source, at once. This permanent and those
 * entering with it are not on the battlefield yet, so none of them is sacrificed; a land that will have lost its
 * abilities as it enters (Blood Sun) sacrifices nothing.
 */
public record SacrificeAllPermanentsAsEntersEffect(PermanentPredicate filter) implements ReplacementEffect {
}
