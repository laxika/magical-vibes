package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;

/**
 * Grant a keyword to the targeted permanent until end of turn, but only if
 * it has the specified supertype (e.g. {@link CardSupertype#LEGENDARY}).
 * <p>
 * The boost from the parent spell still applies unconditionally; only the
 * keyword grant is conditional on the supertype.
 *
 * @param keyword   the keyword to grant
 * @param supertype the supertype the target must have
 */
public record GrantKeywordToTargetIfSupertypeEffect(
        Keyword keyword,
        CardSupertype supertype
) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
