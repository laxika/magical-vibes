package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Grant a keyword to the targeted permanent until end of turn, but only if
 * it matches the predicate.
 * <p>
 * The boost from the parent spell still applies unconditionally; only the
 * keyword grant is conditional on the predicate.
 *
 * @param keyword the keyword to grant
 * @param predicate the predicate the target must match
 */
public record GrantKeywordToTargetIfPermanentEffect(
        Keyword keyword,
        PermanentPredicate predicate
) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
