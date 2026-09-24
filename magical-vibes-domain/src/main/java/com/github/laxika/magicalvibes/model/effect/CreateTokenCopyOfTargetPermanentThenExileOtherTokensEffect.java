package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates a tracked token copy of the entering permanent, then exiles all other tokens created
 * with the same source permanent. Used by Faerie Artisans.
 */
public record CreateTokenCopyOfTargetPermanentThenExileOtherTokensEffect(
        CreateTokenCopyOfTargetPermanentEffect copyEffect) implements CardEffect {

    /** Creates an artifact copy while retaining the source-token tracking needed by the cleanup. */
    public CreateTokenCopyOfTargetPermanentThenExileOtherTokensEffect(Set<CardType> additionalTypes) {
        this(new CreateTokenCopyOfTargetPermanentEffect(
                List.of(), additionalTypes, null, null, Map.of(), false, false, false, false,
                true, false, null, Set.of()));
    }
}
