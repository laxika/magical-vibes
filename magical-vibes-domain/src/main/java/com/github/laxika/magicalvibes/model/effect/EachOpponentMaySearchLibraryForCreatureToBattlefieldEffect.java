package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Each opponent may search their library for a creature card, put it onto the battlefield,
 * then shuffle. Opponents search in APNAP order (active player first among opponents).
 * The search is optional ("may").
 *
 * <p>Used by Boldwyr Heavyweights.
 */
public record EachOpponentMaySearchLibraryForCreatureToBattlefieldEffect(List<UUID> remainingSearchers)
        implements CardEffect {

    public EachOpponentMaySearchLibraryForCreatureToBattlefieldEffect {
        remainingSearchers = remainingSearchers == null ? null : List.copyOf(remainingSearchers);
    }

    public EachOpponentMaySearchLibraryForCreatureToBattlefieldEffect() {
        this(null);
    }
}
