package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.Collection;

/** Describes a static replacement that changes the count of matching created tokens. */
public interface TokenCreationReplacementEffect extends CardEffect {

    /** The token subtype this replacement applies to, or {@code null} for every token. */
    CardSubtype affectedSubtype();

    /** Applies this replacement to the current count of tokens in the creation event. */
    int replaceTokenCount(int currentCount);

    /** Determines the deterministic order used when several token-count replacements apply. */
    default int replacementOrder() {
        return 0;
    }

    default boolean appliesTo(Collection<CardSubtype> tokenSubtypes) {
        return affectedSubtype() == null
                || (tokenSubtypes != null && tokenSubtypes.contains(affectedSubtype()));
    }
}
