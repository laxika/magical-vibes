package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;

/**
 * Grant a keyword to the targeted permanent until end of turn, but only if
 * it has the specified subtype (e.g. {@link CardSubtype#VAMPIRE}).
 * <p>
 * The boost from the parent spell still applies unconditionally; only the
 * keyword grant is conditional on the subtype.
 *
 * @param keyword the keyword to grant
 * @param subtype the subtype the target must have
 */
public record GrantKeywordToTargetIfSubtypeEffect(
        Keyword keyword,
        CardSubtype subtype
) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
