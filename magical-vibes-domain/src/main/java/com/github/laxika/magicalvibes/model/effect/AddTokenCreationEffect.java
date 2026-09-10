package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** Static replacement that adds a fixed number of matching tokens to a creation event. */
public record AddTokenCreationEffect(int additionalTokens, CardSubtype affectedSubtype)
        implements TokenCreationReplacementEffect {

    @Override
    public int replaceTokenCount(int currentCount) {
        return currentCount + additionalTokens;
    }
}
