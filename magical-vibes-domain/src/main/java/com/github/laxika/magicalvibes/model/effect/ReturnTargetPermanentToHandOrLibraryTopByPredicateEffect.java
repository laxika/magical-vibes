package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Returns a targeted permanent to its owner's hand. If the permanent matches
 * {@code libraryTopCondition}, it is put on top of its owner's library instead
 * (a destination replacement, not an additional effect). Used by Consign to Dream
 * ("Return target permanent to its owner's hand. If that permanent is red or green,
 * put it on top of its owner's library instead.").
 *
 * @param libraryTopCondition when the target matches, it goes to the top of the
 *                            library instead of the hand
 */
public record ReturnTargetPermanentToHandOrLibraryTopByPredicateEffect(
        PermanentPredicate libraryTopCondition) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
