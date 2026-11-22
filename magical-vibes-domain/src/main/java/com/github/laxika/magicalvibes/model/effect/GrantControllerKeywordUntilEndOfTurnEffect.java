package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

/**
 * One-shot effect: the controller has the given targeting keyword until end of turn, or for the stated
 * {@code duration} ("until your next turn", Blossoming Calm).
 */
public record GrantControllerKeywordUntilEndOfTurnEffect(Keyword keyword, Duration duration) implements CardEffect {

    public GrantControllerKeywordUntilEndOfTurnEffect(Keyword keyword) {
        this(keyword, Duration.UNTIL_END_OF_TURN);
    }

    public GrantControllerKeywordUntilEndOfTurnEffect {
        if (keyword != Keyword.SHROUD && keyword != Keyword.HEXPROOF) {
            throw new IllegalArgumentException(
                    "GrantControllerKeywordUntilEndOfTurnEffect supports only SHROUD and HEXPROOF, got " + keyword);
        }
    }
}
