package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

/** One-shot effect: the controller has the given targeting keyword until their next turn. */
public record GrantControllerKeywordUntilNextTurnEffect(Keyword keyword) implements CardEffect {

    public GrantControllerKeywordUntilNextTurnEffect {
        if (keyword != Keyword.SHROUD && keyword != Keyword.HEXPROOF) {
            throw new IllegalArgumentException(
                    "GrantControllerKeywordUntilNextTurnEffect supports only SHROUD and HEXPROOF, got " + keyword);
        }
    }
}
