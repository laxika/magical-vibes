package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

/**
 * One-shot effect: the controller has the given targeting keyword until end of turn.
 */
public record GrantControllerKeywordUntilEndOfTurnEffect(Keyword keyword) implements CardEffect {

    public GrantControllerKeywordUntilEndOfTurnEffect {
        if (keyword != Keyword.SHROUD && keyword != Keyword.HEXPROOF) {
            throw new IllegalArgumentException(
                    "GrantControllerKeywordUntilEndOfTurnEffect supports only SHROUD and HEXPROOF, got " + keyword);
        }
    }
}
