package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.UUID;

/**
 * Grants a keyword to the creature chosen earlier during resolution on the same stack entry.
 * When {@code chosenCreatureId} is null, the handler reads the entry's chosen permanent ID.
 * Supports the standard end-of-turn and next-turn one-shot durations.
 */
public record GrantKeywordToChosenCreatureEffect(Keyword keyword, GrantDuration duration,
                                                 UUID chosenCreatureId) implements CardEffect {

    public GrantKeywordToChosenCreatureEffect(Keyword keyword, GrantDuration duration) {
        this(keyword, duration, null);
    }
}
